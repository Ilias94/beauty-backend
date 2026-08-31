package pl.ib.beauty.model.dto;

import lombok.Builder;
import pl.ib.beauty.model.dao.TicketStatus;
import pl.ib.beauty.model.dao.TicketType;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record TicketDto(
        Long id,
        String title,
        String description,
        TicketType type,
        TicketStatus status,
        Long authorId,
        String authorName,
        Long assignedToId,
        String assignedToName,
        List<TicketReplyDto> replies,
        TicketResolutionDto resolution,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
