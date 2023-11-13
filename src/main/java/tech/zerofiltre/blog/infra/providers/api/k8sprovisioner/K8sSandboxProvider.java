package tech.zerofiltre.blog.infra.providers.api.k8sprovisioner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.sandbox.SandboxProvider;
import tech.zerofiltre.blog.domain.sandbox.model.Sandbox;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.infra.InfraProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class K8sSandboxProvider implements SandboxProvider {
    private final RetryTemplate retryTemplate;
    private final RestTemplate restTemplate;
    private final InfraProperties infraProperties;
    private final UserProvider userProvider;


    @Override
    public Sandbox initialize(String fullName, String email) throws ZerofiltreException {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", infraProperties.getK8sProvisionerToken());
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");

        String body = "{\n" +
                "    \"full_name\":\"" + fullName + "\",\n" +
                "    \"email\":\"" + email + "\"\n" +
                "}";

        try {
            log.info("Initializing a k8s sandbox for user {} with request body: \n {}", fullName, body);
            return retryTemplate.execute(retryContext -> {
                String url = infraProperties.getK8sProvisionerUrl() + "/provisioner";
                HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
                ResponseEntity<Sandbox> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Sandbox.class);
                Sandbox result = response.getBody();
//                Optional<User> user = userProvider.userOfEmail(email);
//                user.ifPresent(foundUser -> {
//                    String appUrl = ZerofiltreUtils.getOriginUrl(infraProperties.getEnv());
//                });
                log.info("K8s sandbox for user {} initialized: {}", fullName, result);
                return result;
            });
        } catch (Exception e) {
            throw new ZerofiltreException("We couldn't init k8s sandbox for user " + fullName + "/" + email, e, null);
        }
    }

    @Override
    public void destroy(String fullName, String email) {
        throw new NotImplementedException("Please provide an implementation for this method in: " + this.getClass().getCanonicalName());
    }

}
