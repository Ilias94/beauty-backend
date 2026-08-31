package pl.ib.beauty.model.dto;

import java.time.LocalDateTime;

public record CourseMessageDtoResponse(
        Long id,
        Long courseId,
        Long senderId,
        String senderFirstName,
        String senderLastName,
        String senderFileName,
        String content,
        LocalDateTime sentAt
) {}
