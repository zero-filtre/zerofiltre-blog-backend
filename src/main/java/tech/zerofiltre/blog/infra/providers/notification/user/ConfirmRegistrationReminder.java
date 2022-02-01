package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.*;
import org.springframework.context.*;
import org.springframework.core.env.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import tech.zerofiltre.blog.domain.user.*;

import java.util.*;

import static tech.zerofiltre.blog.util.ZerofiltreUtils.getOriginUrl;

@Component
@RequiredArgsConstructor
public class ConfirmRegistrationReminder {

    private final UserProvider userProvider;
    private final BlogEmailSender emailSender;
    private final MessageSource messages;
    private final VerificationTokenManager tokenManager;
    private final Environment environment;

    @Scheduled(fixedRateString = "${zerofiltre.infra.rate}",initialDelayString = "${zerofiltre.infra.initial-delay}")
    public void remindConfirmRegistration() {

        userProvider.nonActiveUsers()
                .forEach(user -> {
                    if (user.getEmail() != null) {

                        String language = user.getLanguage() != null ? user.getLanguage() : Locale.FRANCE.getLanguage();
                        Locale locale = new Locale(language);

                        String subject = messages.getMessage("message.registration.subject.remind", null, locale);

                        String token = tokenManager.generateToken(user);

                        String firstName = StringUtils.capitalize(user.getFirstName());
                        String lastName = user.getLastName().toUpperCase();

                        String message = messages.getMessage(
                                "message.registration.success.remind.content",
                                new Object[]{firstName, lastName},
                                locale
                        );

                        String pageUri = "/user/accountConfirmation?token=";

                        String greetings = messages.getMessage("message.greetings", null, locale);

                        String url = getOriginUrl(environment) + pageUri + token;

                        String emailContent = message + "\r\n" + url + "\r\n" + greetings;
                        emailSender.send(user.getEmail(), subject, emailContent);
                    }
                });

    }
}
