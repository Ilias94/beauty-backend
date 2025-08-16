package pl.ib.beauty.model.dao;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import jakarta.persistence.*;
import lombok.Data;

import java.io.IOException;

@Entity
@Data
public class Category implements IdentifiedDataSerializable {
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

    @Override
    public int getFactoryId() {
        return 1;
    }

    @Override
    public int getClassId() {
        return 3;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(id != null ? id : -1L); // Sentinel dla null ID
        out.writeString(label);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        long readId = in.readLong();
        id = (readId != -1L) ? readId : null;

        label = in.readString();
    }
}
