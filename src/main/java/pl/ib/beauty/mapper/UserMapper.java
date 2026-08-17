package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Role;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dao.UserCsv;
import pl.ib.beauty.model.dto.UserDtoRequest;
import pl.ib.beauty.model.dto.UserDtoResponse;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper extends AuditableMapper<User, UserDtoResponse> {
    @Mapping(source = "roleList", target = "roles", qualifiedByName = "listRoleToNames")
    @Mapping(source = "createdCourses", target = "ownedCourseIds", qualifiedByName = "coursesToIds")
    @Mapping(source = "coursesParticipating", target = "enrolledCourseIds", qualifiedByName = "setCoursesToIds")
    @Mapping(source = "roleList", target = "isTeacher", qualifiedByName = "hasTeacherRole")
    UserDtoResponse userToDto(User user);

    User userDtoToUser(UserDtoRequest userDto);

    @Named("listRoleToNames")
    default List<String> listRoleToNames(Set<Role> roleList) {
        if (roleList == null) {
            return List.of();
        }
        return roleList.stream()
                .map(Role::getName)
                .toList();
    }

    @Named("hasTeacherRole")
    default boolean hasTeacherRole(Set<Role> roleList) {
        if (roleList == null) return false;
        return roleList.stream().anyMatch(r -> "TEACHER".equals(r.getName()));
    }

    @Named("coursesToIds")
    default List<Long> courseToIds(List<Course> createdCourses) {
        if (createdCourses == null) {
            return List.of();
        }
        return createdCourses.stream()
                .map(Course::getId)
                .toList();
    }

    @Named("setCoursesToIds")
    default List<Long> setCoursesToIds(Set<Course> courses) {
        if (courses == null) {
            return List.of();
        }
        return courses.stream()
                .map(Course::getId)
                .toList();
    }

    UserCsv userToCsv(User user);

    List<UserDtoResponse> userToDtoList(List<User> users);
}
