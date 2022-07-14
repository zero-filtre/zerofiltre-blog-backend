package tech.zerofiltre.blog.domain.user.model;

public class JwtToken {

    private String accessToken;
    private long expiryDateInSeconds;

    public JwtToken() {
    }

    public JwtToken(String accessToken, long expiryDateInSeconds) {
        this.accessToken = accessToken;
        this.expiryDateInSeconds = expiryDateInSeconds;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpiryDateInSeconds() {
        return expiryDateInSeconds;
    }

    public void setExpiryDateInSeconds(long expiryDateInSeconds) {
        this.expiryDateInSeconds = expiryDateInSeconds;
    }
}
