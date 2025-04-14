package pl.ib.beauty.model.dao;

import jakarta.persistence.*;
import jakarta.validation.Constraint;
import lombok.Data;

@Entity
@Data
@Table(indexes = @Index(name = "idx_template_name", columnList = "name", unique = true))
public class Template {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    private String subject;
}
