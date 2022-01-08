package tech.zerofiltre.blog.infra.entrypoints.rest.config;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.web.servlet.error.*;
import org.springframework.context.annotation.*;

@Configuration
public class WebErrorConfiguration {

    @Value("${zerofiltre.infra.entrypoints.rest.api-version}")
    private String currentApiVersion;


    /**
     * We override the default {@link DefaultErrorAttributes}
     *
     * @return A custom implementation of ErrorAttributes
     */
    @Bean
    public ErrorAttributes errorAttributes() {
        return new BlogErrorAttributes(currentApiVersion);
    }

}