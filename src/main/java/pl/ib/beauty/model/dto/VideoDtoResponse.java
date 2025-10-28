package pl.ib.beauty.model.dto;

import lombok.Builder;

@Builder
public record VideoDtoResponse(String url,
                               String description,
                               String title,
                               Long id
) {
}
