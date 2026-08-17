package pl.ib.beauty.model.dao;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.NotAudited;
import pl.ib.beauty.model.CourseLevel;
import pl.ib.beauty.model.CourseStatus;
import pl.ib.beauty.model.CourseType;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(schema = "beautypg")
public class Course implements IdentifiedDataSerializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int maxParticipants;
    private Double rating;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CourseType courseType = CourseType.IN_PERSON;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CourseLevel courseLevel = CourseLevel.BEGINNER;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CourseStatus status = CourseStatus.PUBLISHED;

    @Builder.Default
    private String language = "pl";

    @Column(columnDefinition = "TEXT")
    private String prerequisites;

    @Column(columnDefinition = "TEXT")
    private String learningOutcomes;

    @Builder.Default
    private boolean certificate = false;

    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    @NotAudited
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_course_id")
    @NotAudited
    private Course parentCourse;

    @ManyToMany(mappedBy = "coursesParticipating")
    @NotAudited
    private List<User> participants = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Override
    public int getFactoryId() {
        return 1;
    }

    @Override
    public int getClassId() {
        return 1;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(id);
        out.writeString(title);
        out.writeString(description);
        out.writeObject(startDate);
        out.writeObject(endDate);
        out.writeInt(maxParticipants);
        out.writeObject(rating);
        out.writeObject(price);
        out.writeObject(creator.getId());
        out.writeObject(creator.getFileName());
        out.writeObject(address);
        out.writeObject(category);
        out.writeString(courseType != null ? courseType.name() : CourseType.IN_PERSON.name());
        out.writeObject(parentCourse != null ? parentCourse.getId() : null);
        // fields added in V28
        out.writeString(courseLevel != null ? courseLevel.name() : CourseLevel.BEGINNER.name());
        out.writeString(status != null ? status.name() : CourseStatus.PUBLISHED.name());
        out.writeString(language);
        out.writeString(prerequisites);
        out.writeString(learningOutcomes);
        out.writeBoolean(certificate);
        out.writeString(imageUrl);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        id = in.readLong();
        title = in.readString();
        description = in.readString();
        startDate = in.readObject();
        endDate = in.readObject();
        maxParticipants = in.readInt();
        rating = in.readObject();
        price = in.readObject();
        Long creatorId = in.readObject();
        if (creatorId != null) {
            String filePath = in.readObject();
            this.creator = User.builder()
                    .id(creatorId)
                    .fileName(filePath)
                    .build();
        }
        address = in.readObject();
        category = in.readObject();
        String courseTypeName = in.readString();
        this.courseType = courseTypeName != null ? CourseType.valueOf(courseTypeName) : CourseType.IN_PERSON;
        Long parentCourseId = in.readObject();
        if (parentCourseId != null) {
            this.parentCourse = Course.builder().id(parentCourseId).build();
        }
        // fields added in V28 — read only if available (cache backward-compat)
        try {
            String levelName = in.readString();
            this.courseLevel = levelName != null ? CourseLevel.valueOf(levelName) : CourseLevel.BEGINNER;
            String statusName = in.readString();
            this.status = statusName != null ? CourseStatus.valueOf(statusName) : CourseStatus.PUBLISHED;
            this.language = in.readString();
            this.prerequisites = in.readString();
            this.learningOutcomes = in.readString();
            this.certificate = in.readBoolean();
            this.imageUrl = in.readString();
        } catch (Exception ignored) {
            this.courseLevel = CourseLevel.BEGINNER;
            this.status = CourseStatus.PUBLISHED;
            this.language = "pl";
        }
    }
}
