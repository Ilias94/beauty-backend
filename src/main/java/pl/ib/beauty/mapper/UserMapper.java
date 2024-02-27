package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.UserDto;

@Mapper(componentModel = "spring")
public interface UserMapper extends AuditableMapper<User, UserDto> {
    @Mapping(target = "password", ignore = true)
    UserDto userToDto(User user);

    User userDtoToUser(UserDto userDto);
}
