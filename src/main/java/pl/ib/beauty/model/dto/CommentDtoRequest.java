package pl.ib.beauty.model.dto;

import lombok.Builder;

import java.time.LocalDateTime;
@Builder
public record CommentDtoRequest(String content,
                                LocalDateTime createdDate,
                                LocalDateTime lastModifiedDate,
                                Long courseId,
                                String authorName,
                                Long parentId) {
}
