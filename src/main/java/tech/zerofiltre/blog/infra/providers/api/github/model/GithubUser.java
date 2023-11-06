package tech.zerofiltre.blog.infra.providers.api.github.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubUser {

    private long id;
    private String login;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    @JsonProperty("html_url")
    private String htmlUrl;
    private String bio;
    private String email;
    private String name;
    @JsonProperty("twitter_username")
    private String twitterUserName;
    private String blog;

}
