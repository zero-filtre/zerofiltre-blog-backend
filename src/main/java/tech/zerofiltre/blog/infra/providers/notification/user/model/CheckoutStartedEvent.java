package tech.zerofiltre.blog.infra.providers.notification.user.model;

import org.springframework.context.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public class CheckoutStartedEvent extends ApplicationEvent {
    private String id;
    private String appUrl;
    private Locale locale;
    private User user;
    private Action action;

    public CheckoutStartedEvent(User user, Locale locale, String appUrl) {
        super(user);
        this.id = UUID.randomUUID().toString();
        this.user = user;
        this.locale = locale;
        this.appUrl = appUrl;
        this.action = Action.CHECKOUT_STARTED;
    }

    public String getId() {
        return id;
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

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "CheckoutStartedEvent{" +
                "id='" + id + '\'' +
                ", appUrl='" + appUrl + '\'' +
                ", locale=" + locale +
                ", user=" + user.getFullName() +
                ", action=" + action +
                '}';
    }
}
