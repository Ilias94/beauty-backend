package pl.ib.beauty.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.CourseMessage;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.CourseMessageDtoRequest;
import pl.ib.beauty.model.dto.CourseMessageDtoResponse;
import pl.ib.beauty.model.dto.NotificationDto;
import pl.ib.beauty.repository.CourseMessageRepository;
import pl.ib.beauty.repository.CourseRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseMessageService {

    private final CourseMessageRepository courseMessageRepository;
    private final CourseRepository courseRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<CourseMessageDtoResponse> getMessages(Long courseId) {
        User current = userService.currentLoginUser();
        Course course = courseRepository.findById(courseId).orElseThrow(EntityNotFoundException::new);
        checkAccess(current, course);
        return courseMessageRepository.findByCourseId(courseId).stream().map(this::toDto).toList();
    }

    @Transactional
    public CourseMessageDtoResponse send(Long courseId, CourseMessageDtoRequest request) {
        User sender = userService.currentLoginUser();
        Course course = courseRepository.findById(courseId).orElseThrow(EntityNotFoundException::new);
        checkAccess(sender, course);

        CourseMessage saved = courseMessageRepository.save(
                CourseMessage.builder()
                        .course(course)
                        .sender(sender)
                        .content(request.content())
                        .build()
        );

        CourseMessageDtoResponse response = toDto(saved);
        notificationService.broadcastCourseMessage(courseId, response);

        String senderName = sender.getFirstName() + " " + sender.getLastName();
        String preview = request.content().length() > 60
                ? request.content().substring(0, 60) + "…"
                : request.content();
        NotificationDto notification = new NotificationDto(course.getTitle(), senderName + ": " + preview);

        if (!course.getCreator().getId().equals(sender.getId())) {
            notificationService.sendNotification(course.getCreator().getEmail(), notification);
        }
        course.getParticipants().stream()
                .filter(p -> !p.getId().equals(sender.getId()))
                .forEach(p -> notificationService.sendNotification(p.getEmail(), notification));

        return response;
    }

    private void checkAccess(User user, Course course) {
        boolean isCreator = course.getCreator().getId().equals(user.getId());
        boolean isParticipant = course.getParticipants().stream().anyMatch(p -> p.getId().equals(user.getId()));
        if (!isCreator && !isParticipant) {
            throw new AccessDeniedException("Not a participant of this course");
        }
    }

    private CourseMessageDtoResponse toDto(CourseMessage m) {
        return new CourseMessageDtoResponse(
                m.getId(),
                m.getCourse().getId(),
                m.getSender().getId(),
                m.getSender().getFirstName(),
                m.getSender().getLastName(),
                m.getSender().getFileName(),
                m.getContent(),
                m.getSentAt()
        );
    }
}
