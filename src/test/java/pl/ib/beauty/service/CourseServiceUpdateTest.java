package pl.ib.beauty.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pl.ib.beauty.model.CourseLevel;
import pl.ib.beauty.model.CourseStatus;
import pl.ib.beauty.model.CourseType;
import pl.ib.beauty.model.dao.Category;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.repository.CategoryRepository;
import pl.ib.beauty.repository.CourseRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseServiceUpdateTest {

    private final CourseRepository courseRepository = mock(CourseRepository.class);
    private final CategoryRepository categoryRepository = mock(CategoryRepository.class);
    private final CourseService service = new CourseService(courseRepository, categoryRepository,
            mock(UserService.class), mock(GeoCodingService.class));

    private final Category lashes = category(3L, "Lash Technician");
    private Course existing;

    @BeforeEach
    void setUp() {
        existing = Course.builder().id(16L).title("Rzęsy").price(new BigDecimal("450.00")).maxParticipants(12)
                .courseLevel(CourseLevel.BEGINNER).certificate(false).participants(new ArrayList<>()).build();
        when(courseRepository.findById(16L)).thenReturn(Optional.of(existing));
        when(courseRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        when(categoryRepository.findByLabel("Lash Technician")).thenReturn(Optional.of(lashes));
    }

    @Test
    void savesEveryFieldTheWizardEdits() {
        Course edited = edit(10);

        Course saved = service.updateCourse(16L, edited);

        assertThat(saved.getTitle()).isEqualTo("Stylizacja rzęs 1:1");
        assertThat(saved.getPrice()).isEqualByComparingTo("520.00");
        assertThat(saved.getCourseLevel()).isEqualTo(CourseLevel.INTERMEDIATE);
        assertThat(saved.getLanguage()).isEqualTo("en");
        assertThat(saved.getPrerequisites()).isEqualTo("Własna pęseta");
        assertThat(saved.getLearningOutcomes()).isEqualTo("Technika 1:1");
        assertThat(saved.isCertificate()).isTrue();
        assertThat(saved.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
        // The wizard sends the category by its label only.
        assertThat(saved.getCategory()).isSameAs(lashes);
    }

    @Test
    void theGroupCantShrinkBelowThePeopleAlreadyEnrolled() {
        existing.setParticipants(new ArrayList<>(List.of(new User(), new User(), new User())));

        assertThatThrownBy(() -> service.updateCourse(16L, edit(2)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(courseRepository, never()).save(any());
    }

    private Course edit(int maxParticipants) {
        return Course.builder().title("Stylizacja rzęs 1:1").description("Opis")
                .startDate(LocalDateTime.of(2026, 10, 28, 10, 0)).endDate(LocalDateTime.of(2026, 10, 29, 17, 0))
                .maxParticipants(maxParticipants).courseType(CourseType.ONLINE).price(new BigDecimal("520.00"))
                .courseLevel(CourseLevel.INTERMEDIATE).status(CourseStatus.PUBLISHED).language("en")
                .prerequisites("Własna pęseta").learningOutcomes("Technika 1:1").certificate(true)
                .category(category(null, "Lash Technician"))
                .build();
    }

    private static Category category(Long id, String label) {
        Category category = new Category(label);
        category.setId(id);
        return category;
    }
}
