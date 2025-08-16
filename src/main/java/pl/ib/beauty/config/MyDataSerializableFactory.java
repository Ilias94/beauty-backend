package pl.ib.beauty.config;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import pl.ib.beauty.model.dao.Address;
import pl.ib.beauty.model.dao.Category;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.User;

public class MyDataSerializableFactory implements DataSerializableFactory {
    @Override
    public IdentifiedDataSerializable create(int classId) {
        return switch (classId) {
            case 1 -> new Course();
            case 2 -> new Address();
            case 3 -> new Category();
            default -> null;
        };
    }
}
