package tech.zerofiltre.blog.infra.security.config;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;

@Data
@Component
public class StackOverflowTokenConfiguration {

    @Value("${security.jwt.header:Authorization}")
    private String header;

    @Value("${security.jwt.prefix:Bearer }")
    private String prefix;
}
