package pl.ib.beauty.model.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketReply {
    private String id;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String content;
    private boolean aiGenerated;
    private String createdAt;
}
