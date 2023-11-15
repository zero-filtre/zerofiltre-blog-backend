package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ZerofiltreEmailSender {

    private final JavaMailSender mailSender;
    private final InfraProperties infraProperties;
    private final ITemplateEngine emailTemplateEngine;


    public void send(Email email) {
        try {
            Resource resourceFile = new ClassPathResource("mail_header_image.png");
            String replyTo = email.getReplyTo() == null || email.getReplyTo().isEmpty() ? infraProperties.getContactEmail() : email.getReplyTo();

            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(replyTo);
            helper.setTo(email.getRecipients().toArray(new String[0]));
            helper.setBcc(email.getBccs().toArray(new String[0]));
            helper.setCc(email.getCcs().toArray(new String[0]));
            helper.setSubject(email.getSubject());
            helper.setText(email.getContent(), true);
            helper.setReplyTo(replyTo);
            helper.addInline("attachement.png", resourceFile);
            this.mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            log.error("An error occurred when sending email", e);
        }
    }

    public void send(Email email, boolean templateReady) {
        if (!templateReady) {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("content", email.getContent());
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);
            thymeleafContext.setLocale(Locale.FRENCH);
            String emailContent = emailTemplateEngine.process("general_message.html", thymeleafContext);
            email.setContent(emailContent);
        }
        send(email);
    }

}
