package pl.ib.beauty.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.service.UserService;


@Service
@RequiredArgsConstructor
public class SecurityService {
    private final UserService userService;

    public boolean hasAccessToUser(Long id) {
        User user = userService.currentLoginUser();
        return user.getId().equals(id);
    }
}
