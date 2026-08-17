package pl.ib.beauty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ib.beauty.model.dao.Payment;

import java.util.Optional;
import java.util.UUID;


public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(UUID orderId);
}
