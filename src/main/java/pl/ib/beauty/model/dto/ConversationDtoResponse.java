package pl.ib.beauty.model.dto;

import java.time.LocalDateTime;

public record ConversationDtoResponse(
        Long partnerId,
        String partnerFirstName,
        String partnerLastName,
        String partnerFileName,
        String lastMessage,
        LocalDateTime lastMessageAt,
        long unreadCount
) {}
