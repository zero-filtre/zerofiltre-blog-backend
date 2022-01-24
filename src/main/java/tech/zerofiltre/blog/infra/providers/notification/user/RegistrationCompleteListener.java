package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

@Component
@RequiredArgsConstructor
public class RegistrationCompleteListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final MessageSource messages;
    private final BlogEmailSender emailSender;
    private final VerificationTokenManager tokenManager;


    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = event.isRepeated() ? tokenManager.updateToken(user) : tokenManager.generateToken(user);

        String recipientAddress = user.getEmail();
        String subject = messages.getMessage("message.registration.subject", null, event.getLocale());
        String confirmationUrl
                = event.getAppUrl() + "/user/registrationConfirm?token=" + token;
        String firstName = StringUtils.capitalize(user.getFirstName());
        String lastName = user.getLastName().toUpperCase();
        String message = messages.getMessage("message.registration.success", new Object[]{firstName, lastName}, event.getLocale());
        String greetings = messages.getMessage("message.registration.greetings", null, event.getLocale());
        String emailContent = message + "\r\n" + confirmationUrl + "\r\n" + greetings;

        emailSender.send(recipientAddress, subject, emailContent);
    }


}
