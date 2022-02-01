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
    @Value("${security.jwt.uri:/auth/**}")
    private String uri;

    @Value("${security.jwt.header:Authorization}")
    private String header;

    @Value("${security.jwt.prefix:Bearer }")
    private String prefix;

    @Value("${security.jwt.expiration:#{15*60}}")
    private int expiration;

    @Value("${security.jwt.secret:JwtSecretKey}")
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
