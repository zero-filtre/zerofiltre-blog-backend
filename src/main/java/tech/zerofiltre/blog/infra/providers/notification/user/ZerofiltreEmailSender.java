package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

import javax.mail.*;
import javax.mail.internet.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ZerofiltreEmailSender {

    private final JavaMailSender mailSender;
    private final InfraProperties infraProperties;

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
}
