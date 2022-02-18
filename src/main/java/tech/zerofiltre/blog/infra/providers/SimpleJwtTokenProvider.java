package tech.zerofiltre.blog.infra.providers;

import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.security.model.*;

@Component
public class SimpleJwtTokenProvider implements JwtTokenProvider {

    private final JwtAuthenticationTokenProperties jwtAuthenticationTokenProperties;

    public SimpleJwtTokenProvider(JwtAuthenticationTokenProperties jwtAuthenticationTokenProperties) {
        this.jwtAuthenticationTokenProperties = jwtAuthenticationTokenProperties;
    }

    @Override
    public JwtToken generate(User user) {
        return jwtAuthenticationTokenProperties.buildToken(user.getEmail(), user.getRoles());

    }
}
