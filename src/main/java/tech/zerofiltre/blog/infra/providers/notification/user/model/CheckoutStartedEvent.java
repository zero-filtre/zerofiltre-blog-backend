package tech.zerofiltre.blog.infra.providers.notification.user.model;

import lombok.ToString;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Locale;

@ToString(callSuper = true)
public class CheckoutStartedEvent extends ActionApplicationEvent {

    public CheckoutStartedEvent(User user, Locale locale, String appUrl) {
        super(user, locale, appUrl, null, Action.CHECKOUT_STARTED);
    }


}
