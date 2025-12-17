package pl.ib.beauty.service;

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
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
}
