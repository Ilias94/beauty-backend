package pl.ib.beauty.security;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
@Service
public class TokenService {
    public  JwtClaimsSet getJwtClaimsSet(String email, String roles) {
        return JwtClaimsSet.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .subject(email)
                .claim("scope", roles)
                .build();
    }
}
