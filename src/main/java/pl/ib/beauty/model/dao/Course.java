package pl.ib.beauty.model.dao;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
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
    private int ratingCount;
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

    // The participants themselves aren't cached, only how many there are; null on a course read from the database.
    @Transient
    private Integer cachedParticipantCount;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    /** How many people are enrolled: from the cache entry for a cached course, else from its participants. */
    public int participantCount() {
        if (cachedParticipantCount != null) {
            return cachedParticipantCount;
        }
        return participants != null ? participants.size() : 0;
    }

    /** The count, or -1 when the participants weren't loaded and the session that could load them is gone. */
    private int participantCountIfLoaded() {
        if (cachedParticipantCount == null && participants != null && !Hibernate.isInitialized(participants)) {
            try {
                return participants.size();
            } catch (LazyInitializationException e) {
                return -1;
            }
        }
        return participantCount();
    }

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
        // creator's name, so cached courses still show who teaches them
        out.writeString(creator.getFirstName());
        out.writeString(creator.getLastName());
        // added in V31
        out.writeInt(ratingCount);
        // Without it a cached course looked empty: no seats taken, nobody enrolled.
        out.writeInt(participantCountIfLoaded());
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
            return;
        }
        // creator's name — entries cached before it was added simply don't have it
        try {
            String firstName = in.readString();
            String lastName = in.readString();
            if (this.creator != null) {
                this.creator.setFirstName(firstName);
                this.creator.setLastName(lastName);
            }
        } catch (Exception ignored) {
            // older cache entry
            return;
        }
        try {
            this.ratingCount = in.readInt();
        } catch (Exception ignored) {
            // cached before V31
            return;
        }
        try {
            int count = in.readInt();
            this.cachedParticipantCount = count >= 0 ? count : null;
        } catch (Exception ignored) {
            // cached before the participant count was; left null, so the count comes from the (empty) list
        }
    }
}
