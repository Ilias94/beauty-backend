package pl.ib.beauty.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RatingDto(float value, Long courseId, Float instructorRating) {
}
