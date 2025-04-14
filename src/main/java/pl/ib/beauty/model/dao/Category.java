package pl.ib.beauty.model.dao;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String label;

    public Category(String label) {
        this.label = label;
    }

    public Category() {
    }
}
