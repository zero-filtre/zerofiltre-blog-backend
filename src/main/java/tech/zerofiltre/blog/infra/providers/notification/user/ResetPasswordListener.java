package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

@Component
@RequiredArgsConstructor
public class ResetPasswordListener implements ApplicationListener<OnResetPasswordEvent> {

    private final MessageSource messages;
    private final BlogEmailSender mailSender;
    private final VerificationTokenManager tokenManager;


    @Override
    public void onApplicationEvent(OnResetPasswordEvent event) {
        this.resetPassword(event);
    }

    private void resetPassword(OnResetPasswordEvent event) {
        User user = event.getUser();
        String token = tokenManager.generateToken(user);

        String recipientAddress = user.getEmail();
        String subject = messages.getMessage("message.reset.subject", null, event.getLocale());
        String resetUrl
                = event.getAppUrl() + "/user/changePassword?token=" + token;
        String firstName = StringUtils.capitalize(user.getFirstName());
        String lastName = user.getLastName().toUpperCase();
        String message = messages.getMessage("message.reset.content", new Object[]{firstName, lastName}, event.getLocale());
        String greetings = messages.getMessage("message.registration.greetings", null, event.getLocale());
        String emailContent = message + "\r\n" + resetUrl + "\r\n" + greetings;

        mailSender.send(recipientAddress, subject, emailContent);
    }
}
