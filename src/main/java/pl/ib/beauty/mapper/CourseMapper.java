package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.CourseDto;
import pl.ib.beauty.model.dto.UserDto;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface CourseMapper {
    @Mapping(source = "creator", target = "creator", qualifiedByName = "creator")
    CourseDto courseToDto(Course course);

    Course dtoToCourse(CourseDto courseDto);

    @Named("creator")
    default UserDto creator(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .fileName(user.getFileName())
                .build();
    }
}

