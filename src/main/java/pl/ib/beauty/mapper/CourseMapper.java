package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.CourseDtoRequest;
import pl.ib.beauty.model.dto.CourseDtoResponse;
import pl.ib.beauty.model.dto.UserDtoResponse;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface CourseMapper {
    @Mapping(source = "creator", target = "creator", qualifiedByName = "creator")
    CourseDtoResponse courseToDto(Course course);

    Course dtoToCourse(CourseDtoRequest courseDto);

    @Named("creator")
    default UserDtoResponse creator(User user) {
        if (user == null) return null;
        return UserDtoResponse.builder()
                .id(user.getId())
                .fileName(user.getFileName())
                .build();
    }
}

