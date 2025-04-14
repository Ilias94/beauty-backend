package pl.ib.beauty.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.ib.beauty.model.dao.Comment;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Rating;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.repository.CommentRepository;
import pl.ib.beauty.repository.RatingRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;
    private final CourseService courseService;
    private final UserService userService;

    public Rating saveRating(Long courseId, float value) {
        Course courseById = courseService.getCourseById(courseId);
        User currentLoginUser = userService.currentLoginUser();

        Rating rating = ratingRepository.findByCourseIdAndAuthorId(courseId, currentLoginUser.getId())
                .orElseGet(() -> Rating.builder()
                        .course(courseById)
                        .author(currentLoginUser)
                        .build());

        rating.setValue(value);

        return ratingRepository.save(rating);
    }

    public Rating getCurrentUserRatingByCourseId(Long courseId) {
        User currentLoginUser = userService.currentLoginUser();

        return ratingRepository.findByCourseIdAndAuthorId(courseId, currentLoginUser.getId())
                .orElseGet(() -> Rating.builder()
                        .value(0)
                        .build());
    }

}
