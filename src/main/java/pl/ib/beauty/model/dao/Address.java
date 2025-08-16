package pl.ib.beauty.model.dao;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address implements IdentifiedDataSerializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String district;
    private String street;
    private String streetNumber;
    private String apartmentNumber;
    private String city;
    private String postalCode;
    private Double lat;
    private Double lng;

    @Override
    public int getFactoryId() {
        return 1;
    }

    @Override
    public int getClassId() {
        return 2;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        //todo usunac ifa i zostawic nulla
        out.writeLong(id != null ? id : -1L); // -1 jako wartość domyślna dla null
        out.writeString(district);
        out.writeString(street);
        out.writeString(streetNumber);
        out.writeString(apartmentNumber);
        out.writeString(city);
        out.writeString(postalCode);
        out.writeObject(lat);
        out.writeObject(lng);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        long readId = in.readLong();
        id = (readId != -1L) ? readId : null;

        district = in.readString();
        street = in.readString();
        streetNumber = in.readString();
        apartmentNumber = in.readString();
        city = in.readString();
        postalCode = in.readString();
        lat = in.readObject();
        lng = in.readObject();
    }
}
