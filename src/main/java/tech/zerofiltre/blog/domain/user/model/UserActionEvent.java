package tech.zerofiltre.blog.domain.user.model;

import java.util.*;

public class UserActionEvent {
    private String appUrl;
    private Locale locale;
    private User user;

    public UserActionEvent(String appUrl, Locale locale, User user) {
        this.appUrl = appUrl;
        this.locale = locale;
        this.user = user;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
