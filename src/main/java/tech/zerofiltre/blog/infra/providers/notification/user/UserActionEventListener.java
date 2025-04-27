package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;
import tech.zerofiltre.blog.infra.providers.notification.user.model.UserActionApplicationEvent;
import tech.zerofiltre.blog.infra.security.config.EmailValidator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static tech.zerofiltre.blog.util.ZerofiltreUtils.getOriginUrl;

@Component
@RequiredArgsConstructor
public class UserActionEventListener implements ApplicationListener<UserActionApplicationEvent> {

    private final MessageSource messages;
    private final ZerofiltreEmailSender emailSender;
    private final ITemplateEngine emailTemplateEngine;
    private final InfraProperties infraProperties;



    @Override
    public void onApplicationEvent(UserActionApplicationEvent event) {
        this.handleEvent(event);
    }

    private void handleEvent(UserActionApplicationEvent event) {
        User user = event.getUser();

        boolean validEmail = user.getEmail() != null && EmailValidator.validateEmail(user.getEmail());
        String emailAddress = validEmail ? user.getEmail() : user.getPaymentEmail();
        if (emailAddress != null) {

            String token = event.getToken();

            boolean isPasswordResetAction = event.getAction().equals(Action.PASSWORD_RESET);
            String subjectCode = isPasswordResetAction ?
                    "message.reset.subject" : "message.registration.subject";

            String pageUri = isPasswordResetAction ?
                    "/user/passwordReset?token=" : "/user/accountConfirmation?token=";

            String template = isPasswordResetAction ?
                    "password_reset_confirmation.html" : "account_confirmation.html";

            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("fullName", user.getFullName());
            templateModel.put("validationLink", event.getAppUrl() + pageUri + token);
            templateModel.put("originUrl", getOriginUrl(infraProperties.getEnv()));

            String recipientAddress = user.getEmail();
            String subject = messages.getMessage(subjectCode, null, event.getLocale());
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);
            thymeleafContext.setLocale(event.getLocale());
            String emailContent = emailTemplateEngine.process(template, thymeleafContext);

            Email email = new Email();
            email.setSubject(subject);
            email.setContent(emailContent);
            email.setRecipients(Collections.singletonList(recipientAddress));
            emailSender.send(email, true);
        }
    }

}
