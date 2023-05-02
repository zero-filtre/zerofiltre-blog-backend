package tech.zerofiltre.blog.infra.providers.api.so;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import lombok.extern.slf4j.*;
import org.apache.commons.lang3.*;
import org.springframework.cache.annotation.*;
import org.springframework.http.*;
import org.springframework.retry.support.*;
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
    private final RetryTemplate retryTemplate;


    public StackOverflowLoginProvider(RestTemplate restTemplate, InfraProperties infraProperties, RetryTemplate retryTemplate) {
        this.restTemplate = restTemplate;
        this.infraProperties = infraProperties;
        apiUrl = infraProperties.getStackOverflowAPIRootURL() + infraProperties.getStackOverflowAPIVersion();
        this.retryTemplate = retryTemplate;
    }

    @Cacheable(value = "so-token-validity", key = "#token")
    public boolean isValid(String token) {
        Map<String, String> uriVariables = Collections.singletonMap("key", infraProperties.getStackOverflowAPIKey());
        String urlTemplate = buildURITemplate(apiUrl + "/access-tokens/" + token, uriVariables.keySet());

        try {
            return retryTemplate.execute(retryContext -> {
                ResponseEntity<String> response = restTemplate.getForEntity(urlTemplate, String.class, uriVariables);

                if (response.getStatusCode().is2xxSuccessful()) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = null;
                    try {
                        root = mapper.readTree(response.getBody());
                        JsonNode items = root.path("items");
                        JsonNode node = items.get(0);
                        if (node == null) {
                            log.error(THE_TOKEN_IS_NO_MORE_VALID_DUE_TO, response.getBody());
                            return false;
                        }
                        JsonNode expiresOnDate = node.get("expires_on_date");
                        if (expiresOnDate != null && LocalDateTime.ofEpochSecond(expiresOnDate.longValue(), 0, ZoneOffset.UTC).isBefore(LocalDateTime.now())) {
                            log.error("We couldn't validate the token because it is expired ");
                            return false;
                        }
                    } catch (JsonProcessingException e) {
                        log.error("We couldn't validate the token ", e);
                        return false;
                    }
                } else {
                    log.error(THE_TOKEN_IS_NO_MORE_VALID_DUE_TO, response.getBody());
                    return false;
                }
                return true;
            });
        } catch (Exception e) {
            log.error("We couldn't validate the token ", e);
            return false;
        }

    }

    @Cacheable(value = "so-user", key = "#token")
    public Optional<User> userOfToken(String token) {
        try {
            return retryTemplate.execute(retryContext -> {
                log.debug("Trying to get Stackoverflow user info from opaque token");
                StackOverflowUser stackOverflowUser;
                Map<String, String> uriVariables = new HashMap<>();
                uriVariables.put("site", SITE);
                uriVariables.put("order", "desc");
                uriVariables.put("sort", "reputation");
                uriVariables.put("access_token", token);
                uriVariables.put("filter", "default");
                uriVariables.put("key", infraProperties.getStackOverflowAPIKey());

                String urlTemplate = buildURITemplate(apiUrl + "/me", uriVariables.keySet());

                ResponseEntity<String> response = restTemplate.getForEntity(urlTemplate, String.class, uriVariables);
                if (response.getStatusCode().is2xxSuccessful()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(response.getBody());
                        JsonNode items = root.path("items");
                        JsonNode node = items.get(0);
                        if (node != null) {
                            stackOverflowUser = mapper.treeToValue(node, StackOverflowUser.class);
                            User user = fromStackOverflowUser(stackOverflowUser);
                            return Optional.of(user);
                        }
                    } catch (JsonProcessingException e) {
                        log.error("We couldn't find a user ", e);
                        return Optional.empty();
                    }
                }
                log.error("We couldn't find a user because stackoverflow returned this {} ", response.getBody());
                return Optional.empty();
            });
        } catch (Exception e) {
            log.error("We couldn't find a user ", e);
            return Optional.empty();
        }
    }

    @Override
    public String tokenFromCode(String accessCode) {
        throw new NotImplementedException("Please provide an implementation for this method in: " + this.getClass().getCanonicalName());
    }


    private User fromStackOverflowUser(StackOverflowUser stackOverflowUser) {
        User user = new User();
        user.setEmail(stackOverflowUser.getUserId());
        user.setProfilePicture(stackOverflowUser.getProfileImage());
        user.setFullName(stackOverflowUser.getDisplayName());
        user.setWebsite(stackOverflowUser.getWebsiteUrl());
        Set<SocialLink> socialLinks = Collections.singleton(
                new SocialLink(SocialLink.Platform.STACKOVERFLOW, stackOverflowUser.getLink())
        );
        user.setSocialLinks(socialLinks);
        user.setLoginFrom(SocialLink.Platform.STACKOVERFLOW);
        user.setActive(true);
        return user;
    }

    private String buildURITemplate(String url, Set<String> parameterNames) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        parameterNames.forEach(paramName -> builder.queryParam(paramName, "{" + paramName + "}"));
        return builder.encode().toUriString();
    }


}
