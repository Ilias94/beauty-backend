package pl.ib.beauty.model.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@SuperBuilder
@Data
@Audited
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", indexes = @Index(name = "idx_email", columnList = "email", unique = true))
public class User extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    @NotAudited
    private String password;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roleList;
    @OneToMany(mappedBy = "creator")
    @NotAudited
    private List<Course> createdCourses;
    @ManyToMany
    @NotAudited
    @JoinTable(name = "course_participants", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    private Set<Course> coursesParticipating = new HashSet<>();
    private String fileName;
    private String imagePath;
}
