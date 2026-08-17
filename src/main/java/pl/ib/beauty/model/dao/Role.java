package pl.ib.beauty.model.dao;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

@Entity
@Builder
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Table(schema = "beautypg")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
}
