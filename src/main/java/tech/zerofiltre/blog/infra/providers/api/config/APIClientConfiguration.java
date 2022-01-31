package tech.zerofiltre.blog.infra.providers.api.config;

import org.apache.http.impl.client.*;
import org.springframework.context.annotation.*;
import org.springframework.http.client.*;
import org.springframework.web.client.*;

@Configuration
public class APIClientConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                HttpClientBuilder.create().build());
        return new RestTemplate(clientHttpRequestFactory);
    }
}
