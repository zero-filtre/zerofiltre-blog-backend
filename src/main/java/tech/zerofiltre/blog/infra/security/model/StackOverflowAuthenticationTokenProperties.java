package tech.zerofiltre.blog.infra.security.model;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;

@Data
@Component
@EqualsAndHashCode(callSuper = true)
public class StackOverflowAuthenticationTokenProperties extends AuthenticationTokenProperties {

    @Value("${security.jwt.header:Authorization}")
    private String header;

    @Value("${security.jwt.prefix:stack }")
    private String prefix;
}
