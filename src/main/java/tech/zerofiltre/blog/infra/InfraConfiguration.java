package tech.zerofiltre.blog.infra;

import org.springframework.context.annotation.*;
import org.springframework.retry.backoff.*;
import org.springframework.retry.policy.*;
import org.springframework.retry.support.*;
import org.springframework.web.filter.*;

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

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }

}
