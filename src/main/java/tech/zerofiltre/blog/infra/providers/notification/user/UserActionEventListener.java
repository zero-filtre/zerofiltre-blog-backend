package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

@Component
@RequiredArgsConstructor
public class UserActionEventListener implements ApplicationListener<UserActionApplicationEvent> {

    private final MessageSource messages;
    private final BlogEmailSender emailSender;
    private final VerificationTokenManager tokenManager;


    @Override
    public void onApplicationEvent(UserActionApplicationEvent event) {
        this.handleEvent(event);
    }

    private void handleEvent(UserActionApplicationEvent event) {
        User user = event.getUser();
        String token = tokenManager.generateToken(user);

        boolean isPasswordResetAction = event.getAction().equals(Action.PASSWORD_RESET);
        String subjectCode = isPasswordResetAction ?
                "message.reset.subject" : "message.registration.subject";

        String pageUri = isPasswordResetAction ?
                "/user/passwordReset?token=" : "/user/accountConfirmation?token=";

        String messageCode = isPasswordResetAction ?
                "message.reset.content" : "message.registration.success.content";


        String recipientAddress = user.getEmail();
        String subject = messages.getMessage(subjectCode, null, event.getLocale());
        String url = event.getAppUrl() + pageUri + token;
        String firstName = StringUtils.capitalize(user.getFirstName());
        String lastName = user.getLastName().toUpperCase();
        String message = messages.getMessage(messageCode, new Object[]{firstName, lastName}, event.getLocale());
        String greetings = messages.getMessage("message.greetings", null, event.getLocale());
        String emailContent = message + "\r\n" + url + "\r\n" + greetings;

        emailSender.send(recipientAddress, subject, emailContent);
    }

}
