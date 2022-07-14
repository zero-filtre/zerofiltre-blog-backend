package tech.zerofiltre.blog.infra.security.model;

import io.jsonwebtoken.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

@Data
@Component
@EqualsAndHashCode(callSuper = true)
public class JwtAuthenticationTokenProperties extends AuthenticationTokenProperties {
    @Value("${zerofiltre.infra.security.jwt.uri}")
    private String uri;

    @Value("${zerofiltre.infra.security.jwt.header}")
    private String header;

    @Value("${zerofiltre.infra.security.jwt.prefix}")
    private String prefix;

    @Value("${zerofiltre.infra.security.jwt.expiration-seconds}")
    private long expirationInSeconds;

    @Value("${zerofiltre.infra.security.jwt.secret}")
    private String secret;

    public JwtToken buildToken(String email, Set<String> roles) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plusSeconds(expirationInSeconds);
        String accessToken = Jwts.builder()
                .setSubject(email)
                // Convert to list of strings.
                .claim("authorities", roles)
                .setIssuedAt(Date.from(now.toInstant(ZoneOffset.UTC)))
                .setExpiration(Date.from(expiration.toInstant(ZoneOffset.UTC)))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact();
        return new JwtToken(accessToken, expiration.toEpochSecond(ZoneOffset.UTC));
    }


}
