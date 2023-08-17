package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.notification.user.model.ArticleSubmittedEvent;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;
import tech.zerofiltre.blog.infra.security.config.EmailValidator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ArticleSubmittedEventListener implements ApplicationListener<ArticleSubmittedEvent> {

    private final MessageSource messages;
    private final ZerofiltreEmailSender emailSender;
    private final ITemplateEngine emailTemplateEngine;

    public ArticleSubmittedEventListener(MessageSource messages, ZerofiltreEmailSender emailSender, ITemplateEngine emailTemplateEngine) {
        this.messages = messages;
        this.emailSender = emailSender;
        this.emailTemplateEngine = emailTemplateEngine;
    }

    @Override
    public void onApplicationEvent(ArticleSubmittedEvent event) {
        User user = event.getUser();
        boolean validEmail = user.getEmail() != null && EmailValidator.validateEmail(user.getEmail());
        String emailAddress = validEmail ? user.getEmail() : user.getPaymentEmail();
        if (emailAddress != null) {

            String subjectCode = "message.submitted.article.subject";

            String pageUri = "/articles/" + event.getArticle().getId();

            String template = "article_submitted_notification.html";

            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("fullName", user.getFullName());
            templateModel.put("articleLink", event.getAppUrl() + pageUri);

            String subject = messages.getMessage(subjectCode, null, event.getLocale());
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);
            thymeleafContext.setLocale(event.getLocale());
            String emailContent = emailTemplateEngine.process(template, thymeleafContext);

            Email email = new Email();
            email.setSubject(subject);
            email.setContent(emailContent);
            email.setRecipients(Collections.singletonList(emailAddress));
            emailSender.send(email);
        }
    }
}
