package pl.ib.beauty.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Role;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.UserDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {
    private UserMapper userMapper = new UserMapperImpl();

    @Test
    void shouldMapUserToDto() {
        //given
        Course course = new Course(1L, "hair cut", "man hair cut course", LocalDateTime.now(), LocalDateTime.now(), 10, 4.0, BigDecimal.TEN, null, null, null, null);
        User user = new User(1L, "Adam", "Nowak", "adam@gmail.com", "adam123", Set.of(new Role(1L, "user")), List.of(course), null,null);

        //when
        UserDto result = userMapper.userToDto(user);

        //then
        assertNotNull(result);
        assertEquals(1L, user.getId());
        assertEquals("Adam", result.getFirstName());
        assertEquals("Nowak", result.getLastName());
        assertEquals("Nowak", result.getLastName());
        assertEquals("adam@gmail.com", result.getEmail());
        assertNull(result.getPassword());
        assertNull(result.getConfirmPassword());
        assertNotNull(result.getRevisionNumber());
        assertNotNull(List.of(new Role(1L, "user"), result.getRoles()));
        assertFalse(result.isTeacher());
    }


}