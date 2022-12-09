package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

import java.util.*;

@Component
@RequiredArgsConstructor
public class UserActionEventListener implements ApplicationListener<UserActionApplicationEvent> {

    private final MessageSource messages;
    private final BlogEmailSender emailSender;
    private final VerificationTokenProvider tokenProvider;


    @Override
    public void onApplicationEvent(UserActionApplicationEvent event) {
        this.handleEvent(event);
    }

    private void handleEvent(UserActionApplicationEvent event) {
        User user = event.getUser();
        String token = event.getToken();

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
        String firstName = StringUtils.capitalize(user.getFullName());
        String message = messages.getMessage(messageCode, new Object[]{firstName}, event.getLocale());
        String greetings = messages.getMessage("message.greetings", null, event.getLocale());
        String emailContent = message + "\r\n" + url + "\r\n" + greetings;

        Email email = new Email();
        email.setSubject(subject);
        email.setContent(emailContent);
        email.setRecipients(Collections.singletonList(recipientAddress));
        emailSender.send(email);
    }

}
