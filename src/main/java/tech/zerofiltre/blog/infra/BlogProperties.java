package tech.zerofiltre.blog.infra;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;

@Data
@Component
public class BlogProperties {

    @Value("${zerofiltre.infra.entrypoints.rest.allowed-origins-pattern}")
    private String allowedOriginsPattern;

    @Value("${zerofiltre.infra.entrypoints.rest.api-version}")
    private String currentApiVersion;
}
