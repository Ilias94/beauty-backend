package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.ib.beauty.model.dao.Role;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dao.UserCsv;
import pl.ib.beauty.model.dto.UserDtoRequest;
import pl.ib.beauty.model.dto.UserDtoResponse;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper extends AuditableMapper<User, UserDtoResponse> {
    @Mapping(target = "password", ignore = true)
    @Mapping(source = "roleList", target = "roles", qualifiedByName = "listRoleToNames")
    UserDtoResponse userToDto(User user);

    User userDtoToUser(UserDtoRequest userDto);

    @Named("listRoleToNames")
    default List<String> listRoleToNames(Set<Role> roleList) {
        if (roleList == null) {
            return null;
        }
        return roleList.stream()
                .map(Role::getName)
                .toList();
    }

    UserCsv userToCsv(User user);

    List<UserDtoResponse> userToDtoList(List<User> users);
}
