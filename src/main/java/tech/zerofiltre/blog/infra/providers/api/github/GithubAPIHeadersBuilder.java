package tech.zerofiltre.blog.infra.providers.api.github;

import org.springframework.util.*;

import java.nio.charset.*;
import java.util.*;

public class GithubAPIHeadersBuilder {

    private final MultiValueMap<String, String> headers;

    public GithubAPIHeadersBuilder() {
        this.headers = new LinkedMultiValueMap<>();
        headers.add("Accept", "application/vnd.github.v3+json");
    }

    public GithubAPIHeadersBuilder withBasicAuth(String login, String password) {
        String auth = login + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.add("Authorization", authHeader);
        return this;
    }

    public MultiValueMap<String, String> build() {
        return headers;
    }

    public GithubAPIHeadersBuilder withOAuth(String prefix, String token) {
        headers.add("Authorization", prefix + " " + token);
        return this;
    }
}
