package tech.zerofiltre.blog.infra.security.config;

import io.jsonwebtoken.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;

import java.time.*;
import java.util.*;

@Data
@Component
public class JwtConfiguration {
    @Value("${zerofiltre.infra.security.jwt.uri}")
    private String uri;

    @Value("${zerofiltre.infra.security.jwt.header}")
    private String header;

    @Value("${zerofiltre.infra.security.jwt.prefix}")
    private String prefix;

    @Value("${zerofiltre.infra.security.jwt.expiration}")
    private int expiration;

    @Value("${zerofiltre.infra.security.jwt.secret}")
    private String secret;

    public String buildToken(String email, Set<String> roles) {
        LocalDateTime now = LocalDateTime.now();
        return Jwts.builder()
                .setSubject(email)
                // Convert to list of strings.
                .claim("authorities", roles)
                .setIssuedAt(Date.from(now.toInstant(ZoneOffset.UTC)))
                .setExpiration(Date.from(now.plusMinutes(this.getExpiration()).toInstant(ZoneOffset.UTC)))
                .signWith(SignatureAlgorithm.HS512, this.getSecret().getBytes())
                .compact();
    }
}
