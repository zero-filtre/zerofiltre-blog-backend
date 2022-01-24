package tech.zerofiltre.blog.domain.user.model;

import java.util.*;

public class RegistrationCompleteEvent {
    private String appUrl;
    private Locale locale;
    private User user;
    private boolean repeated;

    public RegistrationCompleteEvent(String appUrl, Locale locale, User user, boolean repeated) {
        this.appUrl = appUrl;
        this.locale = locale;
        this.user = user;
        this.repeated = repeated;
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

    public boolean isRepeated() {
        return repeated;
    }

    public void setRepeated(boolean repeated) {
        this.repeated = repeated;
    }
}
