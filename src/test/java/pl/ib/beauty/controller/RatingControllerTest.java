package pl.ib.beauty.controller;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import pl.ib.beauty.MySqlTestContainer;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class RatingControllerTest extends MySqlTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    @Sql(value = {"classpath:sql/course-data.sql", "classpath:sql/user-data.sql"})
    @WithMockUser(username = "test@test.pl")
    void shouldSaveRating() throws Exception {
        mockMvc.perform(post("/api/v1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(IOUtils.toString(resourceLoader.getResource("classpath:json/rating/rating-save-ok.json").getInputStream(), StandardCharsets.UTF_8))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value").exists())
                .andExpect(jsonPath("$.courseId").value(13));
    }

    @Test
    @Sql(value = {"classpath:sql/course-data.sql", "classpath:sql/user-data.sql", "classpath:sql/rating-data.sql"})
    @WithMockUser(username = "test@test.pl")
    void  shouldGetCurrentUserRatingByCourseId() throws Exception {
        mockMvc.perform(get("/api/v1/rating/{courseId}", 13)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(4))
                .andExpect(jsonPath("$.courseId").value(13));
    }


}