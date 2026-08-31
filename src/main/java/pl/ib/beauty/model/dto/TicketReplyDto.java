package pl.ib.beauty.model.dto;

import lombok.Builder;

@Builder
public record TicketReplyDto(
        String id,
        Long authorId,
        String authorName,
        String authorRole,
        String content,
        boolean aiGenerated,
        String createdAt) {
}
