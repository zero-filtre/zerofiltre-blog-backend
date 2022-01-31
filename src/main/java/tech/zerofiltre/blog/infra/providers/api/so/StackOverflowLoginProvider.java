package tech.zerofiltre.blog.infra.providers.api.so;

import com.fasterxml.jackson.databind.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.client.*;
import org.springframework.web.util.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.providers.api.so.model.*;

import java.time.*;
import java.util.*;

@Component
@Slf4j
public class StackOverflowLoginProvider implements SocialLoginProvider {

    public static final String SITE = "stackoverflow";
    public static final String THE_TOKEN_IS_NO_MORE_VALID_DUE_TO = "The token is no more valid due to {} ";
    private final RestTemplate restTemplate;
    private final InfraProperties infraProperties;
    private final String apiUrl;


    public StackOverflowLoginProvider(RestTemplate restTemplate, InfraProperties infraProperties) {
        this.restTemplate = restTemplate;
        this.infraProperties = infraProperties;
        apiUrl = infraProperties.getStackOverflowAPIRootURL() + infraProperties.getStackOverflowAPIVersion();
    }

    public boolean isValid(String token) {
        Map<String, String> uriVariables = Collections.singletonMap("key", infraProperties.getStackOverflowAPIKey());
        String urlTemplate = buildURITemplate(apiUrl + "/access-tokens/" + token, uriVariables.keySet());

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(urlTemplate, String.class, uriVariables);

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode items = root.path("items");
                JsonNode node = items.get(0);
                if (node == null) {
                    log.error(THE_TOKEN_IS_NO_MORE_VALID_DUE_TO, response.getBody());
                    return false;
                }
                LocalDateTime expiryDate = LocalDateTime.ofEpochSecond(node.get("expires_on_date").longValue(), 0, ZoneOffset.UTC);
                if (expiryDate.isBefore(LocalDateTime.now())) {
                    log.error("We couldn't validate the token because it is expired ");
                    return false;
                }
            } else {
                log.error(THE_TOKEN_IS_NO_MORE_VALID_DUE_TO, response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("We couldn't validate the token ", e);
            return false;
        }
        return true;
    }

    public Optional<User> userOfToken(String token) {
        StackOverflowUser stackOverflowUser;
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("site", SITE);
        uriVariables.put("order", "desc");
        uriVariables.put("sort", "reputation");
        uriVariables.put("access_token", token);
        uriVariables.put("filter", "default");
        uriVariables.put("key", infraProperties.getStackOverflowAPIKey());

        String urlTemplate = buildURITemplate(apiUrl + "/me", uriVariables.keySet());

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(urlTemplate, String.class, uriVariables);
            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode items = root.path("items");
                JsonNode node = items.get(0);
                if (node != null) {
                    stackOverflowUser = mapper.treeToValue(node, StackOverflowUser.class);
                    User user = fromStackOverflowUser(stackOverflowUser);
                    return Optional.of(user);
                }
            }
            log.error("We couldn't find a user because stackoverflow returned this {} ", response.getBody());
        } catch (Exception e) {
            log.error("We couldn't find a user ", e);
            return Optional.empty();
        }
        return Optional.empty();
    }


    private User fromStackOverflowUser(StackOverflowUser stackOverflowUser) {
        User user = new User();
        user.setEmail(stackOverflowUser.getUserId());
        user.setProfilePicture(stackOverflowUser.getProfileImage());
        user.setFirstName(stackOverflowUser.getDisplayName());
        user.setWebsite(stackOverflowUser.getWebsiteUrl());
        Set<SocialLink> socialLinks = Collections.singleton(
                new SocialLink(SocialLink.Platform.STACKOVERFLOW, stackOverflowUser.getLink())
        );
        user.setSocialLinks(socialLinks);
        user.setLoginFrom(SocialLink.Platform.STACKOVERFLOW);
        return user;
    }

    private String buildURITemplate(String url, Set<String> parameterNames) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        parameterNames.forEach(paramName -> builder.queryParam(paramName, "{" + paramName + "}"));
        return builder.encode().toUriString();
    }


}
