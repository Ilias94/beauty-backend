package pl.ib.beauty.model.dao;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.NotAudited;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int maxParticipants;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    @NotAudited
    private User creator;

    @ManyToMany(mappedBy = "coursesParticipating")
    @NotAudited
    private Set<User> participants = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    @Enumerated(EnumType.STRING)
    private Category category;
}
