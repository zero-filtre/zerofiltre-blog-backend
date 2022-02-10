package tech.zerofiltre.blog.infra;

import org.springframework.context.annotation.*;
import org.springframework.retry.backoff.*;
import org.springframework.retry.policy.*;
import org.springframework.retry.support.*;

@Configuration
public class InfraConfiguration {

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
