package tech.zerofiltre.blog.infra.providers.api.github.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Data
@ToString
public class GithubAccessToken {
    @JsonProperty("access_token")
    private String accessToken;
    private String scope;
    @JsonProperty("token_type")
    private String tokenType;
}
