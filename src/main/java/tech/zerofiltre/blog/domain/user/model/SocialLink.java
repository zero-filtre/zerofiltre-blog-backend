package tech.zerofiltre.blog.domain.user.model;

import java.io.*;

public class SocialLink implements Serializable {

    private Platform platform;
    private String link;
    private long userId;
    private long id;

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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public enum Platform {
        GITHUB,
        TWITTER,
        LINKEDIN,
        STACKOVERFLOW
    }
}
