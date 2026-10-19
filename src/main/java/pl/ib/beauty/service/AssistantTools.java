package pl.ib.beauty.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Payment;
import pl.ib.beauty.repository.PaymentRepository;
import pl.ib.beauty.security.SecurityUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Read-only tools the assistant can call. Anything about "my" data is resolved from the
 * logged-in user's JWT, never from arguments the model passes, so the model cannot be
 * talked into reading someone else's payments or tickets.
 */
@Component
@RequiredArgsConstructor
public class AssistantTools {

    private static final int MAX_RESULTS = 10;

    private final FaqService faqService;
    private final CourseService courseService;
    private final TicketService ticketService;
    private final PaymentRepository paymentRepository;

    @Tool(description = "Search the platform FAQ for answers about accounts, payments, courses and how the platform works. "
            + "Use it first for general questions.")
    public List<FaqEntry> searchFaq(@ToolParam(description = "The user's question, in their own words") String question) {
        return faqService.findByQuestion(question).stream()
                .map(faq -> new FaqEntry(faq.getQuestion(), faq.getAnswer()))
                .toList();
    }

    @Tool(description = "Search upcoming beauty courses by title and/or city. Leave a parameter empty to ignore it.")
    public List<CourseSummary> searchCourses(
            @ToolParam(description = "Part of the course title, e.g. 'manicure'", required = false) String title,
            @ToolParam(description = "City, e.g. 'Warszawa'", required = false) String city) {
        return courseService.getCoursesByCategoryAndTitle(null, title, city, null,
                        PageRequest.of(0, MAX_RESULTS), false, false, true)
                .map(CourseSummary::of)
                .getContent();
    }

    @Tool(description = "Get the details of one course by its id (description, prerequisites, learning outcomes).")
    public CourseDetails getCourseDetails(@ToolParam(description = "Course id from searchCourses or getMyCourses") Long courseId) {
        return CourseDetails.of(courseService.getCourseById(courseId));
    }

    @Tool(description = "List the courses the logged-in user is enrolled in.")
    public List<CourseSummary> getMyCourses() {
        return courseService.getCoursesByCategoryAndTitle(null, null, null, null,
                        PageRequest.of(0, MAX_RESULTS), false, true, false)
                .map(CourseSummary::of)
                .getContent();
    }

    @Tool(description = "List the logged-in user's recent payments for courses and their status "
            + "(SUCCESS, IN_PROGRESS = not finished yet, FAILED).")
    public List<PaymentSummary> getMyPayments() {
        return paymentRepository.findTop20ByUserEmailOrderByIdDesc(SecurityUtils.getCurrentUserEmail()).stream()
                .map(PaymentSummary::of)
                .toList();
    }

    @Tool(description = "List the logged-in user's support tickets with their status.")
    public List<TicketSummary> getMyTickets() {
        return ticketService.getMyTickets().stream()
                .limit(MAX_RESULTS)
                .map(ticket -> new TicketSummary(ticket.id(), ticket.title(), ticket.type().name(),
                        ticket.status().name(), ticket.createdAt()))
                .toList();
    }

    public record FaqEntry(String question, String answer) {
    }

    public record CourseSummary(Long id, String title, String city, LocalDateTime startDate, BigDecimal price) {
        static CourseSummary of(Course course) {
            String city = course.getAddress() != null ? course.getAddress().getCity() : null;
            return new CourseSummary(course.getId(), course.getTitle(), city, course.getStartDate(), course.getPrice());
        }
    }

    public record CourseDetails(Long id, String title, String description, String category, String city,
                                LocalDateTime startDate, LocalDateTime endDate, BigDecimal price, Double rating,
                                String prerequisites, String learningOutcomes) {
        static CourseDetails of(Course course) {
            return new CourseDetails(course.getId(), course.getTitle(), course.getDescription(),
                    course.getCategory() != null ? course.getCategory().getLabel() : null,
                    course.getAddress() != null ? course.getAddress().getCity() : null,
                    course.getStartDate(), course.getEndDate(), course.getPrice(), course.getRating(),
                    course.getPrerequisites(), course.getLearningOutcomes());
        }
    }

    public record PaymentSummary(Long courseId, String courseTitle, String status) {
        static PaymentSummary of(Payment payment) {
            return new PaymentSummary(payment.getCourse().getId(), payment.getCourse().getTitle(),
                    payment.getStatus().name());
        }
    }

    public record TicketSummary(Long id, String title, String type, String status, LocalDateTime createdAt) {
    }
}
