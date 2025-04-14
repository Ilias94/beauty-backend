package pl.ib.beauty.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/oauth2/authorization", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class Oauth2Controller {
    @GetMapping()
    public void login(@AuthenticationPrincipal OidcUser user) {
        log.info("{}", user);
    }
}
