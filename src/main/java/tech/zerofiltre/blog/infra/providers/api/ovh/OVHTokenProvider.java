package tech.zerofiltre.blog.infra.providers.api.ovh;

import com.fasterxml.jackson.databind.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.retry.support.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import org.springframework.web.client.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.providers.api.ovh.model.*;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OVHTokenProvider {

    private final RestTemplate restTemplate;
    private final InfraProperties infraProperties;
    private final RetryTemplate retryTemplate;


    public OVHToken getToken() throws Exception {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");

        //BUILD THE BODY

        OVHDomain domain = new OVHDomain();
        domain.setId("default");

        OVHUser user = new OVHUser();
        user.setName(infraProperties.getOvhUsername());
        user.setPassword(infraProperties.getOvhPassword());
        user.setDomain(domain);

        OVHPassword password = new OVHPassword();
        password.setUser(user);

        OVHIdentity identity = new OVHIdentity();
        identity.setMethods(List.of("password"));
        identity.setPassword(password);

        OVHAuth auth = new OVHAuth();
        auth.setIdentity(identity);
        Payload body = new Payload();
        body.setAuth(auth);

        //FIRE THE REQUEST
        try {
            return retryTemplate.execute(retryContext -> {
                HttpEntity<Payload> requestEntity = new HttpEntity<>(body, headers);
                ResponseEntity<String> response = restTemplate.exchange(infraProperties.getOvhAuthUrl(), HttpMethod.POST, requestEntity, String.class);
                OVHToken token = new OVHToken();
                HttpHeaders responseHeaders = response.getHeaders();
                String responseBody = response.getBody();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(responseBody);
                JsonNode root = rootNode.get("token");
                token.setExpiresAt(root.get("expires_at").asText());
                token.setAccessToken(responseHeaders.get("x-subject-token").get(0));
                return token;
            });
        } catch (Exception e) {
            log.error("We couldn't get the OVH token", e);
            throw e;
        }
    }


}
