package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.*;
import org.springframework.context.*;
import org.springframework.core.env.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import tech.zerofiltre.blog.domain.user.*;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ConfirmRegistrationReminder {

    private final UserProvider userProvider;
    private final BlogEmailSender emailSender;
    private final MessageSource messages;
    private final VerificationTokenManager tokenManager;
    private final Environment environment;

    @Scheduled(cron = "0 0 8 1/10 * *")//At 08:00 every 10 days
    public void remindConfirmRegistration() {

        userProvider.nonActiveUsers()
                .forEach(user -> {

                    Locale locale = new Locale(user.getLanguage());

                    String subject = messages.getMessage("message.registration.subject.remind", null, locale);

                    String token = tokenManager.generateToken(user);

                    String firstName = StringUtils.capitalize(user.getFirstName());
                    String lastName = user.getLastName().toUpperCase();

                    String message = messages.getMessage(
                            "message.registration.success.remind.content",
                            new Object[]{firstName, lastName},
                            locale
                    );

                    String pageUri = "/accountConfirmation?token=";

                    String greetings = messages.getMessage("message.greetings", null, locale);

                    String url = getAppUrl() + pageUri + token;

                    String emailContent = message + "\r\n" + url + "\r\n" + greetings;
                    emailSender.send(user.getEmail(), subject, emailContent);
                });

    }

    private String getAppUrl() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length != 0) {

            if (profiles[0].equals("prod"))
                return "https://blog.zerofiltre.tech";

            if (!(profiles[0].isEmpty() || profiles[0].isBlank()))
                return "https://blog-" + profiles[0] + ".zerofiltre.tech";
        }
        return "https://blog-dev.zerofiltre.tech";
    }
}
