package tech.zerofiltre.blog.infra;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;

@Data
@Component
public class InfraProperties {

    @Value("${zerofiltre.infra.entrypoints.rest.allowed-origins-pattern}")
    private String allowedOriginsPattern;

    @Value("${zerofiltre.infra.entrypoints.rest.api-version}")
    private String currentApiVersion;

    @Value("${zerofiltre.infra.api.stackoverflow.root-url}")
    private String stackOverflowAPIRootURL;

    @Value("${zerofiltre.infra.api.stackoverflow.version}")
    private String stackOverflowAPIVersion;

    @Value("${zerofiltre.infra.api.stackoverflow.key}")
    private String stackOverflowAPIKey;
}
