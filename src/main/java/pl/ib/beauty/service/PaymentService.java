package pl.ib.beauty.service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.ib.beauty.model.Status;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

import pl.ib.beauty.config.StripeCredentialConfig;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Payment;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.PaymentStatusDtoResponse;
import pl.ib.beauty.repository.PaymentRepository;
import pl.ib.beauty.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class PaymentService {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final PaymentRepository paymentRepository;
    private final CourseService courseService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final StripeService stripeService;
    private final StripeCredentialConfig stripeCredentialConfig;
    private final NotificationSocketService notificationSocketService;

    @SneakyThrows
    public String createPayment(Long courseId) {
        UUID orderId = UUID.randomUUID();
        User user = userService.currentLoginUser();
        Course courseById = courseService.getCourseById(courseId);

        // The payment would go back to the instructor's own account, less the platform's fee.
        if (courseById.getCreator() != null && Objects.equals(courseById.getCreator().getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot enroll in your own course");
        }

        if (courseById.getEndDate() != null && courseById.getEndDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot enroll in a past course");
        }

        if (courseById.getParticipants() != null &&
                courseById.getParticipants().size() >= courseById.getMaxParticipants()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course is full");
        }

        if (courseById.getParticipants() != null && courseById.getParticipants().contains(user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already enrolled in this course");
        }

        // A cached course carries only part of its creator, so the Stripe account is read from the database.
        User instructor = userRepository.findById(courseById.getCreator().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instructor not found"));
        if (instructor.getStripeAccountId() == null || !instructor.isStripeChargesEnabled()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The instructor can't take payments yet");
        }
        BigDecimal platformFee = platformFee(courseById.getPrice(), instructor);

        Payment payment = Payment.builder()
                .orderId(orderId)
                .user(user)
                .course(courseById)
                .amount(courseById.getPrice())
                .currency(StripeService.CURRENCY)
                .platformFee(platformFee)
                .destinationAccount(instructor.getStripeAccountId())
                .status(Status.IN_PROGRESS)
                .build();

        paymentRepository.save(payment);

        log.info("Created new payment with orderId {} for user {} and course {}",
                orderId,
                user.getId(),
                courseById.getId());

        Session session = stripeService.createSession(courseById, orderId, user.getEmail(),
                instructor.getStripeAccountId(), platformFee);
        payment.setStripeSessionId(session.getId());
        paymentRepository.save(payment);
        return session.getUrl();
    }

    /** The platform's cut in zloty: the instructor's own percentage if set, otherwise the default. */
    BigDecimal platformFee(BigDecimal price, User instructor) {
        BigDecimal percent = instructor.getPlatformFeePercent() != null
                ? instructor.getPlatformFeePercent()
                : stripeCredentialConfig.getPlatformFeePercent();
        return price.multiply(percent).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    /** Status of the current user's payment, read by the page Stripe Checkout returns to. */
    public PaymentStatusDtoResponse getPaymentStatus(UUID orderId) {
        User user = userService.currentLoginUser();
        return paymentRepository.findByOrderId(orderId)
                .filter(payment -> Objects.equals(payment.getUser().getId(), user.getId()))
                .map(payment -> new PaymentStatusDtoResponse(payment.getOrderId(), payment.getStatus(),
                        payment.getCourse().getId(), payment.getCourse().getTitle()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        Event event;
        try {
            event = stripeService.constructEvent(payload, signature);
        } catch (SignatureVerificationException e) {
            log.warn("Rejected a Stripe webhook with an invalid signature");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid signature");
        }

        if (event.getType().startsWith("checkout.session.")) {
            handleCheckoutEvent(event.getType(), (Session) stripeService.eventObject(event));
        }
    }

    public void handleCheckoutEvent(String eventType, Session session) {
        Status status = switch (eventType) {
            // BLIK and Przelewy24 can complete the checkout before the money arrives; async_payment_* follows.
            case "checkout.session.completed" -> "paid".equals(session.getPaymentStatus()) ? Status.SUCCESS : null;
            case "checkout.session.async_payment_succeeded" -> Status.SUCCESS;
            case "checkout.session.async_payment_failed", "checkout.session.expired" -> Status.FAILED;
            default -> null;
        };
        if (status == null || session.getClientReferenceId() == null) {
            return;
        }

        UUID orderId = UUID.fromString(session.getClientReferenceId());
        paymentRepository.findByOrderId(orderId).ifPresentOrElse(
                payment -> updateStatus(payment, status, session.getPaymentIntent()),
                () -> log.warn("Stripe event {} for unknown orderId {}", eventType, orderId));
    }

    private void updateStatus(Payment payment, Status status, String paymentIntentId) {
        // Stripe retries and may resend events; only a payment still waiting for its outcome changes.
        if (payment.getStatus() != Status.IN_PROGRESS) {
            return;
        }
        payment.setStatus(status);
        payment.setStripePaymentIntentId(paymentIntentId);
        paymentRepository.save(payment);
        log.info("Payment {} is now {}", payment.getOrderId(), status);

        if (status == Status.SUCCESS) {
            registerUserToCourse(payment);
        }
    }

    private void registerUserToCourse(Payment payment) {

        User user = payment.getUser();
        Course course = payment.getCourse();

        if (course.getParticipants().contains(user)) {
            return;
        }

        course.getParticipants().add(user);
        user.getCoursesParticipating().add(course);

        courseService.saveCourse(course);

        // 🔔 NOTYFIKACJA
        notificationSocketService.notifyUser(
                course.getCreator().getId(),
                "New sign in for course: " + course.getTitle() +
                        " (participant: " + user.getEmail() + ")"
        );
    }

}
