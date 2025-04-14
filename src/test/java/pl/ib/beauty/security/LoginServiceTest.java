package pl.ib.beauty.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import pl.ib.beauty.model.dto.LoginDto;
import pl.ib.beauty.model.dto.TokenDto;

import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private LoginService loginService;

    @Test
    void shouldLogIn() {
        //given
        String password = "password";
        String email = "test@gmail.com";
        String token = "aqr3h";
        LoginDto loginDto = new LoginDto(email, password);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("admin");
        UsernamePasswordAuthenticationToken usernamePasswordAuthorityAuthenticationToken = new UsernamePasswordAuthenticationToken(email, password, List.of(authority));

        when(authenticationManager.authenticate(usernamePasswordAuthenticationToken)).thenReturn(usernamePasswordAuthorityAuthenticationToken);
        Jwt jwtMock = mock(Jwt.class);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwtMock);
        when(jwtMock.getTokenValue()).thenReturn(token);

        //when
        TokenDto result = loginService.login(loginDto);

        //then
        assertNotNull(result);
        assertEquals(token, result.getToken());
    }

    @Test
    void shouldNotLogIn() {
        //given
        String password = "password";
        String email = "test@gmail.com";
        LoginDto loginDto = new LoginDto(email, password);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(email, password);

        when(authenticationManager.authenticate(usernamePasswordAuthenticationToken)).thenThrow(new UsernameNotFoundException("Wrong credentials for login"));

        //then
        assertThrows(UsernameNotFoundException.class, () -> loginService.login(loginDto));
    }
}