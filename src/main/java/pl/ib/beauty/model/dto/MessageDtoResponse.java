package pl.ib.beauty.model.dto;

import java.time.LocalDateTime;

public record MessageDtoResponse(
        Long id,
        Long senderId,
        Long recipientId,
        String content,
        LocalDateTime sentAt,
        boolean read
) {}
