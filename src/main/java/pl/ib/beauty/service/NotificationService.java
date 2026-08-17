package pl.ib.beauty.service;

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pl.ib.beauty.model.dto.CourseMessageDtoResponse;
import pl.ib.beauty.model.dto.MessageDtoResponse;
import pl.ib.beauty.model.dto.NotificationDto;

@Service
@AllArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void sendNotification(String userEmial, NotificationDto notification) {
        simpMessagingTemplate.convertAndSendToUser(
                userEmial,
                "/queue/notifications",
                notification
        );
    }

    public void sendChatMessage(String recipientEmail, MessageDtoResponse message) {
        simpMessagingTemplate.convertAndSendToUser(
                recipientEmail,
                "/queue/chat",
                message
        );
    }

    public void broadcastCourseMessage(Long courseId, CourseMessageDtoResponse message) {
        simpMessagingTemplate.convertAndSend("/topic/course/" + courseId, message);
    }
}
