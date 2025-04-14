package pl.ib.beauty.model.dao;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.NotAudited;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeLong(id);
        objectDataOutput.writeString(title);
        objectDataOutput.writeString(description);
        objectDataOutput.writeObject(startDate);
        objectDataOutput.writeObject(endDate);
        objectDataOutput.writeInt(maxParticipants);
        objectDataOutput.writeObject(rating);
        objectDataOutput.writeObject(price);
        objectDataOutput.writeInt(maxParticipants);
        objectDataOutput.writeObject(creator.getId());
        objectDataOutput.writeObject(creator.getFileName());
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        id = objectDataInput.readLong();
        title = objectDataInput.readString();
        description = objectDataInput.readString();
        startDate = objectDataInput.readObject();
        endDate = objectDataInput.readObject();
        maxParticipants = objectDataInput.readInt();
        rating = objectDataInput.readObject();
        price = objectDataInput.readObject();
        maxParticipants = objectDataInput.readInt();
        Long creatorId = objectDataInput.readObject();


        if (creatorId != null) {
            String filePath = objectDataInput.readObject();
            User.builder()
                    .id(creatorId)
                    .fileName(filePath)
                    .build();
        }

    }
}
