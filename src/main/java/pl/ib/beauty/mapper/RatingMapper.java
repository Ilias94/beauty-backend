package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.ib.beauty.model.dao.Rating;
import pl.ib.beauty.model.dto.RatingDto;

@Mapper(componentModel = "spring")
public interface RatingMapper {
    @Mapping(source = "course.id", target = "courseId")
    RatingDto ratingToRatingDto(Rating rating);
}
