package tech.zerofiltre.blog.infra.entrypoints.rest.config;

import lombok.extern.slf4j.*;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
import tech.zerofiltre.blog.infra.*;

@Configuration
@Slf4j
public class WebConfiguration implements WebMvcConfigurer {

    private final BlogProperties blogProperties;

    public WebConfiguration(BlogProperties blogProperties) {
        this.blogProperties = blogProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.debug("Registering this allowed origins pattern: {}", blogProperties.getAllowedOriginsPattern());
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedMethods("*")
                .exposedHeaders("*")
                .allowedOriginPatterns(blogProperties.getAllowedOriginsPattern());
    }

}
