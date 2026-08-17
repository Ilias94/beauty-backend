package pl.ib.beauty.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.ib.beauty.model.Status;

import java.time.LocalDateTime;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Payment;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.repository.PaymentRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final CourseService courseService;
    private final UserService userService;
    private final StripeService stripeService;
    private final NotificationSocketService notificationSocketService;

    @SneakyThrows
    public String createPayment(Long courseId) {
        UUID orderId = UUID.randomUUID();
        User user = userService.currentLoginUser();
        Course courseById = courseService.getCourseById(courseId);

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

        Payment payment = Payment.builder()
                .orderId(orderId)
                .user(user)
                .course(courseById)
                .status(Status.IN_PROGRESS)
                .build();

        paymentRepository.save(payment);

        log.info("Created new payment with orderId {} for user {} and course {}",
                orderId,
                user.getId(),
                courseById.getId());

        String sessionUrl = stripeService.createSession(courseById, orderId);
        return sessionUrl;
    }

    public Optional<Payment> updateStatus(UUID orderId, Status status) {
        return paymentRepository.findByOrderId(orderId)
                .map(payment -> {
                    payment.setStatus(status);
                    paymentRepository.save(payment);
                    if (status == Status.SUCCESS) {
                        registerUserToCourse(payment);
                    }
                    return payment;
                });
    }

//    private void registerUserToCourse(Payment payment) {
//
//        User user = payment.getUser();
//        Course course = payment.getCourse();
//
//        if (course.getParticipants().contains(user)) {
//            return;
//        }
//
//
//        course.getParticipants().add(user);
//        user.getCoursesParticipating().add(course);
//
//        courseService.saveCourse(course);
//    }
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
