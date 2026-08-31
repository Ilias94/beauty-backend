package pl.ib.beauty.model.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record FaqDtoResponse(
        UUID id,
        String question,
        String answer,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
