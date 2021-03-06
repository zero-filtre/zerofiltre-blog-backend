package tech.zerofiltre.blog.infra.entrypoints.rest.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.infra.*;


@Configuration
public class OpenAPIConfiguration {

    private final InfraProperties infraProperties;

    public OpenAPIConfiguration(InfraProperties infraProperties) {
        this.infraProperties = infraProperties;
    }

    @Bean
    public OpenAPI zerofiltreBlogOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Zerofiltre Blog APIs")
                        .description("Zerofiltre blog's data management APIs ")
                        .version(infraProperties.getCurrentApiVersion())
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
