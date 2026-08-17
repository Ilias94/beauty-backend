package pl.ib.beauty.controller;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ib.beauty.model.dto.LoginDto;
import pl.ib.beauty.model.dto.TokenDto;
import pl.ib.beauty.security.LoginService;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/login", produces = MediaType.APPLICATION_JSON_VALUE)
public class LoginController {
    private final LoginService loginService;

    @Timed(value = "login.processing.time")
    @Counted(value = "login.requests.count")
    @PostMapping
    TokenDto login(@RequestBody @Valid LoginDto loginDto) {
        return loginService.login(loginDto);
    }
}
