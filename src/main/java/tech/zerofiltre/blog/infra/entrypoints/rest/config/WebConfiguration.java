package tech.zerofiltre.blog.infra.entrypoints.rest.config;

import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@Slf4j
public class WebConfiguration implements WebMvcConfigurer {

    @Value("${zerofiltre.infra.entrypoints.rest.allowed-origins-pattern}")
    private String allowedOriginsPattern;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.debug("Registering this allowed origins pattern: {}", allowedOriginsPattern);
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOriginsPattern);
    }
}
