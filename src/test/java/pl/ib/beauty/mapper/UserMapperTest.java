package pl.ib.beauty.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.ib.beauty.model.dao.Course;
import pl.ib.beauty.model.dao.Role;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.UserDtoResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {
    private final UserMapper userMapper = new UserMapperImpl();

    @Test
    void shouldMapUserToDto() {
        //given
        Course course = new Course(1L, "hair cut", "man hair cut course", LocalDateTime.now(), LocalDateTime.now(), 10, 4.0, BigDecimal.TEN, null, null, null, null);
        User user = new User(1L, "Adam", "Nowak", "adam@gmail.com", "adam123", Set.of(new Role(1L, "user")), List.of(course), null,null,null);

        //when
        UserDtoResponse result = userMapper.userToDto(user);

        //then
        assertNotNull(result);
        assertEquals(1L, user.getId());
        assertEquals("Adam", result.firstName());
        assertEquals("Nowak", result.lastName());
        assertEquals("Nowak", result.lastName());
        assertEquals("adam@gmail.com", result.email());
        assertNull(result.password());
        assertNull(result.confirmPassword());
        assertNotNull(result.revisionNumber());
        assertNotNull(List.of(new Role(1L, "user"), result.roles()));
        assertFalse(result.isTeacher());
    }


}