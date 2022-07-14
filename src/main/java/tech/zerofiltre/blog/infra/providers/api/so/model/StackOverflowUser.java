package tech.zerofiltre.blog.infra.providers.api.so.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowUser {

    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("display_name")
    private String displayName;
    private String link;
    @JsonProperty("profile_image")
    private String profileImage;
    @JsonProperty("website_url")
    private String websiteUrl;

}
