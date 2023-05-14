package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;
import org.thymeleaf.*;
import org.thymeleaf.context.*;
import org.thymeleaf.spring5.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

import java.util.*;

@Component
@RequiredArgsConstructor
public class UserActionEventListener implements ApplicationListener<UserActionApplicationEvent> {

    private final MessageSource messages;
    private final ZerofiltreEmailSender emailSender;
    private final ITemplateEngine emailTemplateEngine;


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

        String template = isPasswordResetAction ?
                "password_reset_confirmation.html" : "account_confirmation.html";

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("fullName", user.getFullName());
        templateModel.put("validationLink", event.getAppUrl() + pageUri + token);

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
        emailSender.send(email);
    }

}
