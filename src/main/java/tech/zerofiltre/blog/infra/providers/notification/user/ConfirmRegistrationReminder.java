package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;
import tech.zerofiltre.blog.infra.security.config.EmailValidator;

import java.util.*;

import static tech.zerofiltre.blog.util.ZerofiltreUtils.getOriginUrl;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmRegistrationReminder {

    private final UserProvider userProvider;
    private final ZerofiltreEmailSender emailSender;
    private final MessageSource messages;
    private final VerificationTokenProvider tokenProvider;
    private final InfraProperties infraProperties;
    private final ITemplateEngine emailTemplateEngine;


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

                        String token = tokenProvider.generate(user).getToken();

                        String pageUri = "/user/accountConfirmation?token=";

                        Map<String, Object> templateModel = new HashMap<>();
                        templateModel.put("fullName", user.getFullName());
                        templateModel.put("validationLink", getOriginUrl(infraProperties.getEnv()) + pageUri + token);
                        templateModel.put("originUrl", getOriginUrl(infraProperties.getEnv()));
                        Context thymeleafContext = new Context();
                        thymeleafContext.setVariables(templateModel);
                        thymeleafContext.setLocale(locale);
                        String emailContent = emailTemplateEngine.process("account_confirmation_reminder.html", thymeleafContext);
                        Email email = new Email();
                        email.setSubject(subject);
                        email.setContent(emailContent);
                        email.setRecipients(Collections.singletonList(user.getEmail()));
                        emailSender.send(email, true);
                    }
                });
        log.info("Triggered {} E-mail addresses confirmation reminders", ref.count);


    }
}
