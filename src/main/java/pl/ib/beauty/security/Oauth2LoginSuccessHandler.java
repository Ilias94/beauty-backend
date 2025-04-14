package pl.ib.beauty.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pl.ib.beauty.model.dao.Role;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.service.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
public class Oauth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserService userService;
    private final JwtEncoder jwtEncoder;
    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.<String>getAttribute("email");
        String name = oAuth2User.<String>getAttribute("name");
        String picturePath = oAuth2User.<String>getAttribute("picture");
        User user = User.builder()
                .email(email)
                .firstName(Objects.requireNonNull(name).split(" ")[0])
                .imagePath(picturePath)
                .build();
        log.info("{}", oAuth2User);
        User savedUser = userService.getByEmail(email).orElseGet(() -> userService.oauth2Save(user));

        String roleNames = savedUser.getRoleList().stream().map(Role::getName)
                .collect(Collectors.joining(" "));

        JwtClaimsSet jwtClaimsSet = tokenService.getJwtClaimsSet(email, roleNames);
        String token = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();
        Cookie cookie = new Cookie("token", token);
        cookie.setMaxAge(60 * 60 * 24);
        cookie.setPath("/");
        response.addCookie(cookie);
        response.addHeader(HttpHeaders.LOCATION, "http://localhost:4200/my-account");
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);

        Cookie dateCookie = new Cookie("currentDate", LocalDateTime.now().toString());
        dateCookie.setMaxAge(60 * 60 * 24);
        dateCookie.setPath("/");
        response.addCookie(dateCookie);
        response.addHeader(HttpHeaders.LOCATION, "http://localhost:4200/my-account");
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    }
}
