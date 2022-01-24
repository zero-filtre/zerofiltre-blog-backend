package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.*;
import org.springframework.context.*;
import org.springframework.mail.*;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

import java.time.*;
import java.util.*;

@Component
@RequiredArgsConstructor
public class RegistrationCompleteListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final VerificationTokenProvider verificationTokenProvider;
    private final MessageSource messages;
    private final JavaMailSender mailSender;


    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = event.isRepeated() ? updateToken(user) : generateToken(user);

        String recipientAddress = user.getEmail();
        String subject = messages.getMessage("message.registration.subject", null, event.getLocale());
        String confirmationUrl
                = event.getAppUrl() + "/user/registrationConfirm?token=" + token;
        String firstName = StringUtils.capitalize(user.getFirstName());
        String lastName = user.getLastName().toUpperCase();
        String message = messages.getMessage("message.registration.success", new Object[]{firstName, lastName}, event.getLocale());
        String greetings = messages.getMessage("message.registration.greetings", null, event.getLocale());
        String emailContent = message + "\r\n" + confirmationUrl + "\r\n" + greetings;

        sendEmail(recipientAddress, subject, emailContent);
    }

    private String generateToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(user, token);
        verificationTokenProvider.save(verificationToken);
        return token;
    }

    private String updateToken(User user) {
        VerificationToken verificationToken = verificationTokenProvider.ofUser(user)
                .map(vToken -> {
                    vToken.setExpiryDate(LocalDateTime.now().plusDays(1));
                    return vToken;
                }).orElse(new VerificationToken(user, UUID.randomUUID().toString()));
        return verificationTokenProvider.save(verificationToken).getToken();
    }

    private void sendEmail(String recipientAddress, String subject, String emailContent) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(emailContent);
        mailSender.send(email);
    }
}
