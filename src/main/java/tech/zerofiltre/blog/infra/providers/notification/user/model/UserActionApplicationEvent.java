package tech.zerofiltre.blog.infra.providers.notification.user.model;

import lombok.ToString;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Locale;

@ToString
public class UserActionApplicationEvent extends ActionApplicationEvent {

    public UserActionApplicationEvent(User user, Locale locale, String appUrl, String token, Action action) {
        super(user, locale, appUrl, token, action);
    }
}
