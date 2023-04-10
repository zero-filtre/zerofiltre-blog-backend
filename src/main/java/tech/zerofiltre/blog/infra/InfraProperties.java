package tech.zerofiltre.blog.infra;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;

@Data
@Component
public class InfraProperties {

    @Value("${zerofiltre.infra.entrypoints.rest.allowed-origins-pattern}")
    private String allowedOriginsPattern;

    @Value("${zerofiltre.infra.entrypoints.rest.api-version}")
    private String currentApiVersion;

    @Value("${zerofiltre.infra.api.stackoverflow.root-url}")
    private String stackOverflowAPIRootURL;

    @Value("${zerofiltre.infra.api.stackoverflow.version}")
    private String stackOverflowAPIVersion;

    @Value("${zerofiltre.infra.api.stackoverflow.key}")
    private String stackOverflowAPIKey;

    @Value("${zerofiltre.infra.api.github.root-url}")
    private String githubAPIRootURL;

    @Value("${zerofiltre.infra.api.github.client-id}")
    private String githubAPIClientId;

    @Value("${zerofiltre.infra.api.github.client-secret}")
    private String githubAPIClientSecret;

    @Value("${zerofiltre.infra.api.vimeo.access-token}")
    private String vimeoAccessToken;

    @Value("${zerofiltre.infra.api.vimeo.root-url}")
    private String vimeoRootURL;

    @Value("${zerofiltre.infra.api.ovh.auth-url}")
    private String ovhAuthUrl;

    @Value("${zerofiltre.infra.api.ovh.username}")
    private String ovhUsername;

    @Value("${zerofiltre.infra.api.ovh.password}")
    private String ovhPassword;

    @Value("${zerofiltre.infra.api.stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${zerofiltre.infra.api.stripe.webhook-secret}")
    private String stripeWebhookSecret;

    @Value("${zerofiltre.infra.api.stripe.customer-portal-link}")
    private String customerPortalLink;

    @Value("${zerofiltre.infra.max-attempts}")
    private int maxAttempts;

    @Value("${zerofiltre.env:dev}")
    private String env;

    @Value("${zerofiltre.contact.email:info@zerofiltre.tech}")
    private String contactEmail;


}
