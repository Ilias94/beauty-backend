package pl.ib.beauty.schedular;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Rating;
import pl.ib.beauty.repository.CourseRepository;
import pl.ib.beauty.repository.RatingRepository;
import pl.ib.beauty.service.CourseService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RatingScheduler {
    private final CourseRepository courseRepository;
    private final RatingRepository ratingRepository;


    @Scheduled(fixedDelay = 60000)
    public void calculateRating() {
        System.out.println("Schedular");
        List<Course> courses = courseRepository.findAll();

        courses.forEach(course -> {
                    List<Rating> ratings = ratingRepository.findByCourseId(course.getId());
                    if (!ratings.isEmpty()) {
                        course.setRating(ratings.stream()
                                .mapToDouble(Rating::getValue)
                                .sum() / ratings.size());
                        courseRepository.saveAll(courses);
                    } else {
                        course.setRating(0.0);
                    }
                }
        );
    }
}
