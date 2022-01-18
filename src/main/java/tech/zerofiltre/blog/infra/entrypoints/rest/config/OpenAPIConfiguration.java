package tech.zerofiltre.blog.infra.entrypoints.rest.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;


@Configuration
public class OpenAPIConfiguration {

    @Value("${zerofiltre.infra.entrypoints.rest.api-version}")
    private String currentApiVersion;

    @Bean
    public OpenAPI zerofiltreBlogOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Zerofiltre Blog APIs")
                        .description("Zerofiltre blog's data management APIs ")
                        .version(currentApiVersion)
                        .license(new License().name("Creative Commons").url("http://creativecommons.org/licenses/by/4.0/"))
                        .contact(
                                new Contact()
                                        .name("Zerofiltre")
                                        .email("info@zerofiltre.tech")
                                        .url("https://zerofiltre.tech")
                        )
                );
    }
}
