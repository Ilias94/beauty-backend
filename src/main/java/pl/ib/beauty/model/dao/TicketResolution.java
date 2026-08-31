package pl.ib.beauty.model.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResolution {
    private String source;
    private String content;
    private Long resolvedById;
    private String resolvedByName;
    private Double confidenceScore;
    private List<Long> matchedDocumentIds;
    private String resolvedAt;
}
