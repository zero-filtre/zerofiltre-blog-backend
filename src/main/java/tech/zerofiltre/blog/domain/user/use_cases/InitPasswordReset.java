package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.logging.model.LogEntry;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public class InitPasswordReset {
    private final UserProvider userProvider;
    private final UserNotificationProvider userNotificationProvider;
    private final VerificationTokenProvider verificationTokenProvider;
    private final LoggerProvider loggerProvider;

    public InitPasswordReset(UserProvider userProvider, UserNotificationProvider userNotificationProvider, VerificationTokenProvider verificationTokenProvider, LoggerProvider loggerProvider) {
        this.userProvider = userProvider;
        this.userNotificationProvider = userNotificationProvider;
        this.verificationTokenProvider = verificationTokenProvider;
        this.loggerProvider = loggerProvider;
    }

    public void execute(String email, String appUrl, Locale locale) throws UserNotFoundException, ResetPasswordNotAllowedException {
        User user = userProvider.userOfEmail(email)
                .orElseThrow(() -> new UserNotFoundException("We were unable to find a user with the corresponding email: " + email, email));

        if(isSocialAccount(user)){
            throw new ResetPasswordNotAllowedException("Github and stackoverflow users not allowed to reset their password", email);
        }

        String token = verificationTokenProvider.generate(user,86400).getToken();
        userNotificationProvider.notify(new UserActionEvent(appUrl, locale, user, token,null, Action.PASSWORD_RESET));

    }

    private boolean isSocialAccount(User user){
        return user.getLoginFrom() != null;
    }
}
