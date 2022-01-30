package tech.zerofiltre.blog.infra.entrypoints.rest.config;

import lombok.extern.slf4j.*;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
import tech.zerofiltre.blog.infra.*;

@Configuration
@Slf4j
public class WebConfiguration implements WebMvcConfigurer {

    private final InfraProperties infraProperties;

    public WebConfiguration(InfraProperties infraProperties) {
        this.infraProperties = infraProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.debug("Registering this allowed origins pattern: {}", infraProperties.getAllowedOriginsPattern());
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedMethods("*")
                .exposedHeaders("*")
                .allowedOriginPatterns(infraProperties.getAllowedOriginsPattern());
    }

}
