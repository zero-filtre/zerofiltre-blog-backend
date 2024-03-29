package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.Domains;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.logging.MessageSourceProvider;
import tech.zerofiltre.blog.domain.logging.model.LogEntry;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

import static tech.zerofiltre.blog.domain.error.ErrorMessages.YOU_ARE_NOT_ALLOWED_TO_READ_THIS_LESSON_AS_THE_COURSE_IS_NOT_YET_PUBLISHED;

public class InitPasswordReset {
    private final UserProvider userProvider;
    private final UserNotificationProvider userNotificationProvider;
    private final VerificationTokenProvider verificationTokenProvider;
    private final MessageSourceProvider messageSourceProvider;

    public InitPasswordReset(UserProvider userProvider, UserNotificationProvider userNotificationProvider, VerificationTokenProvider verificationTokenProvider, MessageSourceProvider messageSourceProvider) {
        this.userProvider = userProvider;
        this.userNotificationProvider = userNotificationProvider;
        this.verificationTokenProvider = verificationTokenProvider;
        this.messageSourceProvider = messageSourceProvider;
    }

    public void execute(String email, String appUrl, Locale locale) throws UserNotFoundException, ForbiddenActionException{
        User user = userProvider.userOfEmail(email)
                .orElseThrow(() -> new UserNotFoundException("We were unable to find a user with the corresponding email: " + email, email));

        if(isSocialAccount(user)){
            throw new ForbiddenActionException(messageSourceProvider.getMessage("ZBLOG_013", null, locale) + email, Domains.USER.name());
        }

        String token = verificationTokenProvider.generate(user,86400).getToken();
        userNotificationProvider.notify(new UserActionEvent(appUrl, locale, user, token,null, Action.PASSWORD_RESET));

    }

    private boolean isSocialAccount(User user){
        return user.getLoginFrom() != null;
    }
}
