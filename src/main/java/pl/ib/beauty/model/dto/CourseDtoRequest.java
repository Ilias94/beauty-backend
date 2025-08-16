package pl.ib.beauty.model.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record CourseDtoRequest(String title,
                               String description,
                               LocalDateTime startDate,
                               LocalDateTime endDate,
                               int maxParticipants,
                               UserDtoRequest creator,
                               AddressDtoResponse address,
                               CategoryDtoResponse category,
                               Double rating,
                               BigDecimal price) {
}
