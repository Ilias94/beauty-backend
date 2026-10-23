package pl.ib.beauty.model.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class CourseDtoRequestJsonTest {

    @Test
    void aNewCoursesAddressComesWithoutCoordinates() {
        // As the course wizard sends it: the backend works out lat/lng, so the browser leaves them out.
        // Jackson 3's defaults (no application.yml) are used on purpose: they reject null for primitives.
        String json = """
                {"title": "Brwi henna", "maxParticipants": 10, "certificate": true,
                 "address": {"street": "Floriańska", "streetNumber": "12", "city": "Kraków"},
                 "category": {"label": "Permanent Makeup Artist"}}""";

        CourseDtoRequest course = JsonMapper.builder().build().readValue(json, CourseDtoRequest.class);

        assertThat(course.address().city()).isEqualTo("Kraków");
        assertThat(course.address().lat()).isNull();
    }
}
