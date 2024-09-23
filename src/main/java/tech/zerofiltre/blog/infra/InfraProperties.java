package tech.zerofiltre.blog.infra;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    @Value("${zerofiltre.infra.api.ovh.bucket-url}")
    private String ovhBucketUrl;

    @Value("${zerofiltre.infra.api.ovh.username}")
    private String ovhUsername;

    @Value("${zerofiltre.infra.api.ovh.password}")
    private String ovhPassword;

    @Value("${zerofiltre.infra.api.stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${zerofiltre.infra.api.stripe.webhook-secret}")
    private String stripeWebhookSecret;

    @Value("${zerofiltre.infra.api.openai.url}")
    private String openaiUrl;

    @Value("${zerofiltre.infra.api.openai.api-key}")
    private String openaiApiKey;

    @Value("${zerofiltre.infra.api.openai.organization-id}")
    private String openaiOrganizationId;

    @Value("${zerofiltre.infra.api.openai.project-id}")
    private String openaiProjectId;

    @Value("${zerofiltre.infra.api.stripe.customer-portal-link}")
    private String customerPortalLink;

    @Value("${zerofiltre.infra.api.stripe.pro-plan-product-id}")
    private String proPlanProductId;

    @Value("${zerofiltre.infra.api.stripe.pro-plan-price-id}")
    private String proPlanPriceId;

    @Value("${zerofiltre.infra.api.stripe.pro-plan-yearly-price-id}")
    private String proPlanYearlyPriceId;

    @Value("${zerofiltre.infra.api.k8s-provisioner.url}")
    private String k8sProvisionerUrl;

    @Value("${zerofiltre.infra.api.k8s-provisioner.token}")
    private String k8sProvisionerToken;

    @Value("${zerofiltre.infra.max-attempts}")
    private int maxAttempts;

    @Value("${zerofiltre.env:dev}")
    private String env;

    @Value("${zerofiltre.infra.sandbox.k8s.doc:https://github.com/Zerofiltre-Courses/bootcamp-devops-dev/blob/main/k8s/k8s_README.md}")
    private String sandboxK8sDoc;

    @Value("${zerofiltre.contact.email:info@zerofiltre.tech}")
    private String contactEmail;

    @Value("${zerofiltre.infra.checkout-reminder-delay-ms:86400000}")
    private long checkoutReminderDelayMs;

    @Value("${zerofiltre.infra.checkout-reminder-check-frequency-ms:3600000}")
    private long checkoutReminderCheckFrequencyMs;

    @Value("${zerofiltre.infra.api.notchpay.url}")
    private String notchPayUrl;

    @Value("${zerofiltre.infra.api.notchpay.public-key}")
    private String notchPayPublicKey;

    @Value("${zerofiltre.infra.api.notchpay.private-key}")
    private String notchPayPrivateKey;

    @Value("${zerofiltre.infra.api.notchpay.hash}")
    private String notchPayHash;


}
