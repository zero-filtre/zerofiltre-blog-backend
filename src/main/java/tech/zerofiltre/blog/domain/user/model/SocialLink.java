package tech.zerofiltre.blog.domain.user.model;

public class SocialLink {

    private Platform platform;
    private String link;

    public SocialLink() {
    }

    public SocialLink(Platform platform, String link) {
        this.platform = platform;
        this.link = link;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public enum Platform {
        GITHUB,
        TWITTER,
        LINKEDIN,
        STACKOVERFLOW
    }
}
