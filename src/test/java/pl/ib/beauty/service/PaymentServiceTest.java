package pl.ib.beauty.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.ArgumentCaptor;
import pl.ib.beauty.config.StripeCredentialConfig;
import pl.ib.beauty.model.Status;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Payment;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.PaymentStatusDtoResponse;
import pl.ib.beauty.repository.PaymentRepository;
import pl.ib.beauty.repository.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final CourseService courseService = mock(CourseService.class);
    private final UserService userService = mock(UserService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final StripeService stripeService = mock(StripeService.class);
    private final StripeCredentialConfig stripeConfig = new StripeCredentialConfig();
    private final NotificationSocketService notificationSocketService = mock(NotificationSocketService.class);

    private final PaymentService service = new PaymentService(paymentRepository, courseService, userService,
            userRepository, stripeService, stripeConfig, notificationSocketService);

    private final User instructor = user(1L, "marta@test.pl");
    private final User student = user(2L, "anna@test.pl");
    private final Course course = Course.builder().id(10L).title("Stylizacja rzęs").creator(instructor)
            .participants(new ArrayList<>()).build();
    private final UUID orderId = UUID.randomUUID();
    private final Payment payment = Payment.builder().orderId(orderId).user(student).course(course)
            .status(Status.IN_PROGRESS).build();

    @BeforeEach
    void setUp() {
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));
    }

    @Test
    void checkoutSendsThePaymentToTheInstructorMinusTheDefaultFee() {
        Course paidCourse = paidCourse("299.00");
        connect(instructor);
        when(stripeService.createSession(any(), any(), anyString(), anyString(), any())).thenReturn(checkoutSession());

        assertThat(service.createPayment(20L)).isEqualTo("https://checkout.stripe.com/c/pay/cs_1");

        ArgumentCaptor<Payment> saved = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, atLeastOnce()).save(saved.capture());
        assertThat(saved.getValue().getPlatformFee()).isEqualByComparingTo("29.90");
        assertThat(saved.getValue().getDestinationAccount()).isEqualTo("acct_marta");
        assertThat(saved.getValue().getStripeSessionId()).isEqualTo("cs_1");
        verify(stripeService).createSession(eq(paidCourse), any(), eq("anna@test.pl"), eq("acct_marta"),
                argThat(fee -> fee.compareTo(new BigDecimal("29.90")) == 0));
    }

    @Test
    void anInstructorsOwnFeeOverridesTheDefault() {
        instructor.setPlatformFeePercent(BigDecimal.ZERO);

        assertThat(service.platformFee(new BigDecimal("299.00"), instructor)).isEqualByComparingTo("0");
    }

    @Test
    void theFeeIsRoundedToGrosze() {
        stripeConfig.setPlatformFeePercent(new BigDecimal("12.5"));

        // 12.5% of 99.99 is 12.49875
        assertThat(service.platformFee(new BigDecimal("99.99"), instructor)).isEqualByComparingTo("12.50");
    }

    @Test
    void noCheckoutWhileTheInstructorCantTakePayments() {
        paidCourse("299.00");
        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));

        assertThatThrownBy(() -> service.createPayment(20L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        verify(paymentRepository, never()).save(any());
        verify(stripeService, never()).createSession(any(), any(), anyString(), anyString(), any());
    }

    @Test
    void paidCheckoutEnrolsTheStudentAndNotifiesTheInstructor() {
        service.handleCheckoutEvent("checkout.session.completed", session("paid"));

        assertThat(payment.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(payment.getStripePaymentIntentId()).isEqualTo("pi_1");
        assertThat(course.getParticipants()).containsExactly(student);
        verify(courseService).saveCourse(course);
        verify(notificationSocketService).notifyUser(anyLong(), anyString());
    }

    @Test
    void checkoutCompletedBeforeTheMoneyArrivedWaitsForTheAsyncResult() {
        service.handleCheckoutEvent("checkout.session.completed", session("unpaid"));

        assertThat(payment.getStatus()).isEqualTo(Status.IN_PROGRESS);
        assertThat(course.getParticipants()).isEmpty();

        service.handleCheckoutEvent("checkout.session.async_payment_succeeded", session("paid"));

        assertThat(payment.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(course.getParticipants()).containsExactly(student);
    }

    @Test
    void expiredCheckoutFailsThePaymentWithoutEnrolling() {
        service.handleCheckoutEvent("checkout.session.expired", session("unpaid"));

        assertThat(payment.getStatus()).isEqualTo(Status.FAILED);
        assertThat(course.getParticipants()).isEmpty();
        verify(courseService, never()).saveCourse(any());
    }

    @Test
    void aResentEventChangesNothing() {
        service.handleCheckoutEvent("checkout.session.completed", session("paid"));
        service.handleCheckoutEvent("checkout.session.completed", session("paid"));
        service.handleCheckoutEvent("checkout.session.expired", session("unpaid"));

        assertThat(payment.getStatus()).isEqualTo(Status.SUCCESS);
        verify(notificationSocketService).notifyUser(anyLong(), anyString());
    }

    @Test
    void webhookWithABadSignatureIsRejected() throws Exception {
        when(stripeService.constructEvent("{}", "t=1,v1=forged"))
                .thenThrow(new SignatureVerificationException("bad", "t=1,v1=forged"));

        assertThatThrownBy(() -> service.handleWebhook("{}", "t=1,v1=forged"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThat(payment.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    void studentReadsTheStatusOfTheirPayment() {
        when(userService.currentLoginUser()).thenReturn(student);

        assertThat(service.getPaymentStatus(orderId))
                .isEqualTo(new PaymentStatusDtoResponse(orderId, Status.IN_PROGRESS, 10L, "Stylizacja rzęs"));
    }

    @Test
    void someoneElsesPaymentIsNotFound() {
        when(userService.currentLoginUser()).thenReturn(instructor);

        assertThatThrownBy(() -> service.getPaymentStatus(orderId))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Course paidCourse(String price) {
        // As cached in Hazelcast: the creator is only its id and name, without the Stripe account.
        Course paidCourse = Course.builder().id(20L).title("Makijaż permanentny").price(new BigDecimal(price))
                .maxParticipants(10).creator(User.builder().id(1L).firstName("Marta").build())
                .participants(new ArrayList<>()).build();
        when(userService.currentLoginUser()).thenReturn(student);
        when(courseService.getCourseById(20L)).thenReturn(paidCourse);
        return paidCourse;
    }

    private void connect(User instructor) {
        instructor.setStripeAccountId("acct_marta");
        instructor.setStripeChargesEnabled(true);
        when(userRepository.findById(instructor.getId())).thenReturn(Optional.of(instructor));
    }

    private static Session checkoutSession() {
        Session session = new Session();
        session.setId("cs_1");
        session.setUrl("https://checkout.stripe.com/c/pay/cs_1");
        return session;
    }

    private Session session(String paymentStatus) {
        Session session = new Session();
        session.setClientReferenceId(orderId.toString());
        session.setPaymentStatus(paymentStatus);
        session.setPaymentIntent("pi_1");
        return session;
    }

    private static User user(Long id, String email) {
        return User.builder().id(id).firstName("Test").lastName("Test").email(email)
                .coursesParticipating(new HashSet<>()).build();
    }
}
