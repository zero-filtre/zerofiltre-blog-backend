package tech.zerofiltre.blog.domain.user.model;

import java.util.*;

public class ResetPasswordEvent extends UserActionEvent {

    public ResetPasswordEvent(String appUrl, Locale locale, User user) {
        super(appUrl, locale, user);
    }
}
