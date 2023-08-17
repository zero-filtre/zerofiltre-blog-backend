package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public class InitPasswordReset {
    private final UserProvider userProvider;
    private final UserNotificationProvider userNotificationProvider;
    private final VerificationTokenProvider verificationTokenProvider;

    public InitPasswordReset(UserProvider userProvider, UserNotificationProvider userNotificationProvider, VerificationTokenProvider verificationTokenProvider) {
        this.userProvider = userProvider;
        this.userNotificationProvider = userNotificationProvider;
        this.verificationTokenProvider = verificationTokenProvider;
    }

    public void execute(String email, String appUrl, Locale locale) throws UserNotFoundException {
        User user = userProvider.userOfEmail(email)
                .orElseThrow(() -> new UserNotFoundException("We were unable to find a user with the corresponding email: " + email, email));
        String token = verificationTokenProvider.generate(user,86400).getToken();
        userNotificationProvider.notify(new UserActionEvent(appUrl, locale, user, token,null, Action.PASSWORD_RESET));

    }
}
