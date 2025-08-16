package pl.ib.beauty.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.ib.beauty.model.Status;
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

    @SneakyThrows
    public String createPayment(Long courseId) {
        UUID orderId = UUID.randomUUID();
        User user = userService.currentLoginUser();
        Course courseById = courseService.getCourseById(courseId);


        Payment payment = Payment.builder()
                .orderId(orderId.toString())
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
        return paymentRepository.findByOrderId(orderId.toString())
                .map(payment -> {
                    payment.setStatus(status);
                    paymentRepository.save(payment);
                    return payment;
                });
    }
}
