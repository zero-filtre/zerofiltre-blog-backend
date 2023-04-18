package tech.zerofiltre.blog.infra.providers.api.config;

import org.apache.http.impl.client.*;
import org.springframework.context.annotation.*;
import org.springframework.http.client.*;
import org.springframework.retry.backoff.*;
import org.springframework.retry.policy.*;
import org.springframework.retry.support.*;
import org.springframework.web.client.*;
import tech.zerofiltre.blog.infra.*;

@Configuration
public class APIClientConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                HttpClientBuilder.create().build());
        return new RestTemplate(clientHttpRequestFactory);
    }

    @Bean
    public RetryTemplate retryTemplate(InfraProperties infraProperties) {
        RetryTemplate retryTemplate = new RetryTemplate();

        BackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        retryTemplate.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(infraProperties.getMaxAttempts());
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
