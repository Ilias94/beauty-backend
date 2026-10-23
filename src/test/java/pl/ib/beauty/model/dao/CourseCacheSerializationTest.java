package pl.ib.beauty.model.dao;

import com.hazelcast.internal.serialization.Data;
import com.hazelcast.internal.serialization.SerializationService;
import com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder;
import org.junit.jupiter.api.Test;
import pl.ib.beauty.config.MyDataSerializableFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CourseCacheSerializationTest {

    // The same serialization the "course" cache uses (factory 1, see HazelcastConfig).
    private final SerializationService serialization = new DefaultSerializationServiceBuilder()
            .addDataSerializableFactory(1, new MyDataSerializableFactory())
            .build();

    @Test
    void aCachedCourseStillKnowsHowManyAreEnrolled() {
        Course course = Course.builder().id(20L).title("Brwi henna").maxParticipants(10).price(new BigDecimal("299"))
                .startDate(LocalDateTime.of(2026, 10, 30, 10, 0))
                .creator(User.builder().id(1L).firstName("Marta").lastName("Instruktorska").build())
                .participants(new ArrayList<>(List.of(new User(), new User()))).build();

        Data cached = serialization.toData(course);
        Course fromCache = serialization.toObject(cached);

        assertThat(fromCache.getParticipants()).isEmpty();
        assertThat(fromCache.participantCount()).isEqualTo(2);
    }

    @Test
    void aCourseFromTheDatabaseCountsItsParticipants() {
        Course course = Course.builder().participants(new ArrayList<>(List.of(new User()))).build();

        assertThat(course.participantCount()).isEqualTo(1);
    }
}
