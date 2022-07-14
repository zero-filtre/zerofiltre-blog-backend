package tech.zerofiltre.blog.infra.security.model;

import lombok.*;

@Data
public class Token {
    private String refreshToken;
    private String accessToken;
    private long accessTokenExpiryDateInSeconds;
    private long refreshTokenExpiryDateInSeconds;
    private String tokenType;

}
