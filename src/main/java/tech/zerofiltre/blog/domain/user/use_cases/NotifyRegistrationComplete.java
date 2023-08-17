package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public class NotifyRegistrationComplete {
    private final UserNotificationProvider userNotificationProvider;

    public NotifyRegistrationComplete(UserNotificationProvider userNotificationProvider) {
        this.userNotificationProvider = userNotificationProvider;
    }

    public void execute(User user, String token, Locale locale, String applicationURL) {
        UserActionEvent registrationCompleteEvent = new UserActionEvent(
                applicationURL,
                locale,
                user,
                token,null, Action.REGISTRATION_COMPLETE);
        userNotificationProvider.notify(registrationCompleteEvent);

    }
}
