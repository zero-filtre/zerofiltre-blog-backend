package tech.zerofiltre.blog.infra.providers.api.ovh.model;

import lombok.*;

@Data
public class OVHToken {
    private String accessToken;
    private String expiresAt;
}
