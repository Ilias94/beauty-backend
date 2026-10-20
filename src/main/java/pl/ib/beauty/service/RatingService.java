package pl.ib.beauty.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.ib.beauty.model.dao.Comment;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Rating;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.RatingSummaryDto;
import pl.ib.beauty.repository.RatingRepository;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;
    private final CourseService courseService;
    private final UserService userService;

    public Rating saveRating(Long courseId, float value, Float instructorRating) {
        Course courseById = courseService.getCourseById(courseId);
        User currentLoginUser = userService.currentLoginUser();

        Rating rating = ratingRepository.findByCourseIdAndAuthorId(courseId, currentLoginUser.getId())
                .orElseGet(() -> Rating.builder()
                        .course(courseById)
                        .author(currentLoginUser)
                        .build());

        rating.setValue(value);
        rating.setInstructorRating(instructorRating);

        Rating saved = ratingRepository.save(rating);

        RatingSummaryDto summary = getSummary(courseId);
        courseService.updateRating(courseId, summary.average(), summary.count());

        if (instructorRating != null && courseById.getCreator() != null) {
            Long instructorId = courseById.getCreator().getId();
            ratingRepository.findAverageInstructorRating(instructorId)
                    .ifPresent(avg -> userService.updateInstructorRating(instructorId, avg));
        }

        return saved;
    }

    public Rating getCurrentUserRatingByCourseId(Long courseId) {
        User currentLoginUser = userService.currentLoginUser();

        return ratingRepository.findByCourseIdAndAuthorId(courseId, currentLoginUser.getId())
                .orElseGet(() -> Rating.builder()
                        .value(0)
                        .build());
    }

    public RatingSummaryDto getSummary(Long courseId) {
        return RatingSummaryDto.of(ratingRepository.findValuesByCourseId(courseId));
    }

}
