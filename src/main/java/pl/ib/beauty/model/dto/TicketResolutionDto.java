package pl.ib.beauty.model.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record TicketResolutionDto(
        String source,
        String content,
        Long resolvedById,
        String resolvedByName,
        Double confidenceScore,
        List<Long> matchedDocumentIds,
        String resolvedAt) {
}
