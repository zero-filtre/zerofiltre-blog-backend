package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.security.config.*;

import java.util.*;

import static tech.zerofiltre.blog.util.ZerofiltreUtils.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmRegistrationReminder {

    private final UserProvider userProvider;
    private final BlogEmailSender emailSender;
    private final MessageSource messages;
    private final VerificationTokenManager tokenManager;
    private final InfraProperties infraProperties;

    @Scheduled(fixedRateString = "${zerofiltre.infra.reminder-rate}", initialDelayString = "${zerofiltre.infra.reminder-initial-delay}")
    public void remindConfirmRegistration() {

        List<User> nonActiveUsers = userProvider.nonActiveUsers();
        log.info("Trying to remind {} inactive users to confirm their E-mail addresses", nonActiveUsers.size());
        var ref = new Object() {
            long count = 0;
        };
        nonActiveUsers
                .forEach(user -> {
                    if (user.getEmail() != null && EmailValidator.validateEmail(user.getEmail())) {
                        ref.count++;

                        String language = user.getLanguage() != null ? user.getLanguage() : Locale.FRANCE.getLanguage();
                        Locale locale = new Locale(language);

                        String subject = messages.getMessage("message.registration.subject.remind", null, locale);

                        String token = tokenManager.generateToken(user);

                        String firstName = user.getFirstName() != null ? StringUtils.capitalize(user.getFirstName()) : "";
                        String lastName = user.getLastName() != null ? user.getLastName().toUpperCase() : "";

                        String message = messages.getMessage(
                                "message.registration.success.remind.content",
                                new Object[]{firstName, lastName},
                                locale
                        );

                        String pageUri = "/user/accountConfirmation?token=";

                        String greetings = messages.getMessage("message.greetings", null, locale);

                        String url = getOriginUrl(infraProperties.getEnv()) + pageUri + token;

                        String emailContent = message + "\r\n" + url + "\r\n" + greetings;
                        emailSender.send(user.getEmail(), subject, emailContent);
                    }
                });
        log.info("Triggered {} E-mail addresses confirmation reminders", ref.count);


    }
}
