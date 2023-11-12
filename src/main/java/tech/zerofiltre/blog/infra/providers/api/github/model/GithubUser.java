package tech.zerofiltre.blog.infra.providers.api.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubUser {

    private String id;
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
