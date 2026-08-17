package pl.ib.beauty.model.dto;

import lombok.Builder;
import pl.ib.beauty.model.CourseLevel;
import pl.ib.beauty.model.CourseStatus;
import pl.ib.beauty.model.CourseType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record CourseDtoRequest(String title,
                               String description,
                               LocalDateTime startDate,
                               LocalDateTime endDate,
                               int maxParticipants,
                               CourseType courseType,
                               CourseLevel courseLevel,
                               CourseStatus status,
                               String language,
                               String prerequisites,
                               String learningOutcomes,
                               boolean certificate,
                               String imageUrl,
                               UserDtoRequest creator,
                               AddressDtoResponse address,
                               CategoryDtoResponse category,
                               Double rating,
                               BigDecimal price) {
}
