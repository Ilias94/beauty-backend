package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dto.CourseDto;
@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface CourseMapper {
    @Mapping(source = "creator.id", target = "creatorId")
    CourseDto courseToDto(Course course);

    //    @Mapping(target = "creator", ignore = true)
    Course dtoToCourse(CourseDto courseDto);
}

