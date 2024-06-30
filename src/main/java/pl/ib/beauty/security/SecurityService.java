package pl.ib.beauty.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.service.CourseService;
import pl.ib.beauty.service.UserService;


@Service
@RequiredArgsConstructor
public class SecurityService {
    private final UserService userService;
    private final CourseService courseService;

    public boolean hasAccessToUser(Long id) {
        User user = userService.currentLoginUser();
        return user.getId().equals(id);
    }

    public boolean isCourseCreator(Long courseId) {
        User currentUser = userService.currentLoginUser();
        Course course = courseService.getCourseById(courseId);
        return course.getCreator().getId().equals(currentUser.getId());
    }
}
