package tech.zerofiltre.blog.infra.providers.notification.user.model;

import org.springframework.context.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public class OnResetPasswordEvent extends ApplicationEvent {
    private String appUrl;
    private Locale locale;
    private User user;

    public OnResetPasswordEvent(User user, Locale locale, String appUrl) {
        super(user);
        this.user = user;
        this.locale = locale;
        this.appUrl = appUrl;

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
