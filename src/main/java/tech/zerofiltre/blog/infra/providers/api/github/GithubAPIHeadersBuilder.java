package tech.zerofiltre.blog.infra.providers.api.github;

import org.springframework.http.*;
import org.springframework.util.*;

import java.nio.charset.*;
import java.util.*;

public class GithubAPIHeadersBuilder {

    private final MultiValueMap<String, String> headers;

    public GithubAPIHeadersBuilder() {
        this.headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.ACCEPT, "application/vnd.github.v3+json");
    }

    public GithubAPIHeadersBuilder withBasicAuth(String login, String password) {
        String auth = login + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.add(HttpHeaders.AUTHORIZATION, authHeader);
        return this;
    }


    public GithubAPIHeadersBuilder withOAuth(String prefix, String token) {
        headers.add(HttpHeaders.AUTHORIZATION, prefix + " " + token);
        return this;
    }

    public GithubAPIHeadersBuilder addHeader(String key, String value) {
        headers.replace(key, Collections.singletonList(value));
        return this;
    }

    public MultiValueMap<String, String> build() {
        return headers;
    }

}
