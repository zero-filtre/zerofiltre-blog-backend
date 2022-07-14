package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public class ResendRegistrationConfirmation {
    private final UserProvider userProvider;
    private final UserNotificationProvider userNotificationProvider;

    public ResendRegistrationConfirmation(UserProvider userProvider, UserNotificationProvider userNotificationProvider) {
        this.userProvider = userProvider;
        this.userNotificationProvider = userNotificationProvider;
    }


    public void execute(String email, String appUrl, Locale locale) throws UserNotFoundException {
        User user = userProvider.userOfEmail(email)
                .orElseThrow(() -> new UserNotFoundException("We were unable to find a user with the corresponding email: " + email, email));

        userNotificationProvider.notify(new UserActionEvent(appUrl, locale, user, Action.REGISTRATION_COMPLETE));

    }

}

