package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.CourseDtoRequest;
import pl.ib.beauty.model.dto.CourseDtoResponse;
import pl.ib.beauty.model.dto.UserDtoResponse;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface CourseMapper {
    @Mapping(source = "creator", target = "creator", qualifiedByName = "creator")
    @Mapping(target = "participantCount", expression = "java(course.getParticipants() != null ? course.getParticipants().size() : 0)")
    @Mapping(source = "parentCourse.id", target = "parentCourseId")
    CourseDtoResponse courseToDto(Course course);

    List<CourseDtoResponse> toDtoList(List<Course> courses);

    Course dtoToCourse(CourseDtoRequest courseDto);

    @Named("creator")
    default UserDtoResponse creator(User user) {
        if (user == null) return null;
        return UserDtoResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fileName(user.getFileName())
                .instructorRating(user.getInstructorRating())
                .build();
    }
}

