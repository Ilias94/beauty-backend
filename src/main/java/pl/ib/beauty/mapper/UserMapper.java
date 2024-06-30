package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.ib.beauty.model.dao.Role;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.UserDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper extends AuditableMapper<User, UserDto> {
    @Mapping(target = "password", ignore = true)
    @Mapping(source = "roleList", target = "roles", qualifiedByName = "listRoleToNames")
    UserDto userToDto(User user);

    User userDtoToUser(UserDto userDto);

    @Named("listRoleToNames")
    default List<String> listRoleToNames(List<Role> roleList) {
        if (roleList == null) {
            return null;
        }
        return roleList.stream()
                .map(Role::getName)
                .toList();
    }
}
