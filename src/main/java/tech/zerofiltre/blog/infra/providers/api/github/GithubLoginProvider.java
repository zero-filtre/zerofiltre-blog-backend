package tech.zerofiltre.blog.infra.providers.api.github;

import lombok.extern.slf4j.*;
import org.springframework.cache.annotation.*;
import org.springframework.http.*;
import org.springframework.retry.support.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import org.springframework.web.client.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.providers.api.github.model.*;

import java.util.*;

@Slf4j
@Component
public class GithubLoginProvider implements SocialLoginProvider {

    private final RestTemplate restTemplate;
    private final InfraProperties infraProperties;
    private final String apiUrl;
    private final String clientId;
    private final String clientSecret;
    private final RetryTemplate retryTemplate;

    public GithubLoginProvider(RestTemplate restTemplate, InfraProperties infraProperties, RetryTemplate retryTemplate) {
        this.restTemplate = restTemplate;
        this.infraProperties = infraProperties;
        this.apiUrl = infraProperties.getGithubAPIRootURL();
        this.clientId = infraProperties.getGithubAPIClientId();
        this.clientSecret = infraProperties.getGithubAPIClientSecret();
        this.retryTemplate = retryTemplate;
    }

    @Override
    @Cacheable(value = "github-token-validity", key = "#token")
    public boolean isValid(String token) {
        try {
            return retryTemplate.execute(retryContext -> {
                String finalUrl = apiUrl + "applications/" + infraProperties.getGithubAPIClientId() + "/token";
                HttpEntity requestEntity = new HttpEntity(
                        Collections.singletonMap("access_token", token),
                        new GithubAPIHeadersBuilder()
                                .withBasicAuth(infraProperties.getGithubAPIClientId(), infraProperties.getGithubAPIClientSecret())
                                .build()
                );
                ResponseEntity<String> response = restTemplate.exchange(finalUrl, HttpMethod.POST, requestEntity, String.class);
                return response.getStatusCode().is2xxSuccessful();
            });
        } catch (Exception e) {
            log.error("We couldn't validate the token", e);
            return false;
        }
    }

    @Override
    //@Cacheable(value = "github-user", key = "#token")
    public Optional<User> userOfToken(String token) {
        try {
            return retryTemplate.execute(retryContext -> {
                log.info("Trying to get github user info from opaque token");
                String finalUrl = apiUrl + "user";
                HttpEntity requestEntity = new HttpEntity(new GithubAPIHeadersBuilder()
                        .withOAuth("token", token)
                        .build());
                ResponseEntity<GithubUser> response = restTemplate.exchange(finalUrl, HttpMethod.GET, requestEntity, GithubUser.class);
                GithubUser githubUser = response.getBody();
                User user = fromGithubUser(githubUser);
                return Optional.of(user);
            });
        } catch (Exception e) {
            log.error("We couldn't find a user ", e);
            return Optional.empty();
        }
    }

    @Override
    public String tokenFromCode(String accessCode) {
        try {
            return retryTemplate.execute(retryContext -> {
                String finalUrl = "https://github.com/login/oauth/access_token" +
                        "?code=" + accessCode +
                        "&client_id=" + clientId +
                        "&client_secret=" + clientSecret;
                HttpEntity requestEntity = new HttpEntity(new GithubAPIHeadersBuilder()
                        .addHeader("Accept", "application/json").build());
                ResponseEntity<GithubAccessToken> response = restTemplate.exchange(finalUrl, HttpMethod.POST, requestEntity, GithubAccessToken.class);
                return response.getBody().getAccessToken();
            });
        } catch (Exception e) {
            log.error("We couldn't retrieve the token ", e);
            return null;
        }
    }

    private User fromGithubUser(GithubUser githubUser) {
        User user = new User();
        user.setEmail(githubUser.getEmail() == null ? githubUser.getLogin() : githubUser.getEmail());
        user.setFullName(githubUser.getName() == null ? StringUtils.capitalize(githubUser.getLogin()) : githubUser.getName());
        user.setProfilePicture(githubUser.getAvatarUrl());
        user.setBio(user.getBio());
        user.setWebsite(githubUser.getBlog());
        Set<SocialLink> socialLinks = new HashSet<>();
        socialLinks.add(new SocialLink(SocialLink.Platform.GITHUB, githubUser.getHtmlUrl()));
        if (githubUser.getTwitterUserName() != null) {
            socialLinks.add(new SocialLink(SocialLink.Platform.TWITTER, "https://twitter.com/" + githubUser.getTwitterUserName()));
        }
        user.setSocialLinks(socialLinks);
        user.setLoginFrom(SocialLink.Platform.GITHUB);
        user.setActive(true);
        return user;
    }
}
