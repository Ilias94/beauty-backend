package pl.ib.beauty.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.repository.QuestionRepository;
import pl.ib.beauty.repository.UserRepository;
import pl.ib.beauty.repository.VideoRepository;
import pl.ib.beauty.service.CourseService;
import pl.ib.beauty.service.UserService;


@Service
@RequiredArgsConstructor
public class SecurityService {
    private final UserService userService;
    private final CourseService courseService;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final QuestionRepository questionRepository;

    public boolean hasAccessToUser(Long id) {
        User user = userService.currentLoginUser();
        return user.getId().equals(id);
    }

    public boolean isCourseCreator(Long courseId) {
        User currentUser = userService.currentLoginUser();
        Course course = courseService.getCourseById(courseId);
        return course.getCreator().getId().equals(currentUser.getId());
    }

    /** The course's paid material (its videos) is for its creator and the people enrolled in it. */
    public boolean canAccessCourseMaterials(Long courseId) {
        User currentUser = userService.currentLoginUser();
        return isCourseCreator(courseId) || userRepository.existsByIdAndCoursesParticipating_Id(currentUser.getId(), courseId);
    }

    /** Took part in the course: what rating and reviewing it requires. */
    public boolean isEnrolled(Long courseId) {
        User currentUser = userService.currentLoginUser();
        return courseId != null && userRepository.existsByIdAndCoursesParticipating_Id(currentUser.getId(), courseId);
    }

    /** The question belongs to a quiz of one of the current user's courses. */
    public boolean isQuestionOwner(Long questionId) {
        User currentUser = userService.currentLoginUser();
        return questionRepository.existsByIdAndCourse_Creator_Id(questionId, currentUser.getId());
    }

    public boolean isVideoOwner(Long videoId) {
        User currentUser = userService.currentLoginUser();
        return videoRepository.existsByIdAndCourse_Creator_Id(videoId, currentUser.getId());
    }
}
