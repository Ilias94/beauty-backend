package pl.ib.beauty.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.ib.beauty.model.Status;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Payment;
import pl.ib.beauty.repository.PaymentRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssistantToolsTest {

    private final CourseService courseService = mock(CourseService.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final AssistantTools tools = new AssistantTools(mock(FaqService.class), courseService,
            mock(TicketService.class), paymentRepository);

    @BeforeEach
    void logIn() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anna@test.pl", null, List.of()));
    }

    @AfterEach
    void logOut() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void myPaymentsAreReadForTheLoggedInUserOnly() {
        Course course = new Course();
        course.setId(7L);
        course.setTitle("Manicure hybrydowy");
        Payment payment = new Payment();
        payment.setCourse(course);
        payment.setStatus(Status.SUCCESS);
        when(paymentRepository.findTop20ByUserEmailOrderByIdDesc("anna@test.pl")).thenReturn(List.of(payment));

        assertThat(tools.getMyPayments())
                .containsExactly(new AssistantTools.PaymentSummary(7L, "Manicure hybrydowy", "SUCCESS"));
    }

    @Test
    void searchCoursesSkipsPastCoursesAndIgnoresTheCurrentUser() {
        when(courseService.getCoursesByCategoryAndTitle(isNull(), eq("manicure"), eq("Kraków"), isNull(),
                any(Pageable.class), eq(false), eq(false), eq(true))).thenReturn(new PageImpl<>(List.of()));

        tools.searchCourses("manicure", "Kraków");

        verify(courseService).getCoursesByCategoryAndTitle(isNull(), eq("manicure"), eq("Kraków"), isNull(),
                any(Pageable.class), eq(false), eq(false), eq(true));
    }

    @Test
    void myCoursesAreTheCoursesTheUserIsEnrolledIn() {
        when(courseService.getCoursesByCategoryAndTitle(isNull(), isNull(), isNull(), isNull(),
                any(Pageable.class), eq(false), eq(true), eq(false))).thenReturn(new PageImpl<>(List.of()));

        assertThat(tools.getMyCourses()).isEmpty();
    }
}
