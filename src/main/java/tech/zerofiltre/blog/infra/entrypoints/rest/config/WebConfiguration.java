package tech.zerofiltre.blog.infra.entrypoints.rest.config;

import lombok.extern.slf4j.*;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
import tech.zerofiltre.blog.infra.*;

@Configuration
@Slf4j
public class WebConfiguration implements WebMvcConfigurer {

    private final BlogConfiguration blogConfiguration;

    public WebConfiguration(BlogConfiguration blogConfiguration) {
        this.blogConfiguration = blogConfiguration;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.debug("Registering this allowed origins pattern: {}", blogConfiguration.getCurrentApiVersion());
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedMethods("*")
                .exposedHeaders("*")
                .allowedOriginPatterns(blogConfiguration.getCurrentApiVersion());
    }

}
