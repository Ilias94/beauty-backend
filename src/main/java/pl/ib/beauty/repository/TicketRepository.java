package pl.ib.beauty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ib.beauty.model.dao.Ticket;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    List<Ticket> findAllByOrderByCreatedAtDesc();
}
