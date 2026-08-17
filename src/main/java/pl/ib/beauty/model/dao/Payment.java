package pl.ib.beauty.model.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.ib.beauty.model.Status;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(schema = "beautypg")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID orderId;
    @ManyToOne
    private User user;
    @ManyToOne
    private Course course;
    @Enumerated(value = EnumType.STRING)
    private Status status;
}
