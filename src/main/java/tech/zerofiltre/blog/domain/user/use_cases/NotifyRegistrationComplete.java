package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public class NotifyRegistrationComplete {
    private final UserNotificationProvider userNotificationProvider;

    public NotifyRegistrationComplete(UserNotificationProvider userNotificationProvider) {
        this.userNotificationProvider = userNotificationProvider;
    }

    public void execute(User user, String applicationURL, Locale locale) {
        RegistrationCompleteEvent registrationCompleteEvent = new RegistrationCompleteEvent(
                applicationURL,
                locale,
                user,
                false);
        userNotificationProvider.notifyRegistrationComplete(registrationCompleteEvent);

    }
}
