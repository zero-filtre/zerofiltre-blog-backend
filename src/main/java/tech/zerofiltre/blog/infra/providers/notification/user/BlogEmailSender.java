package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.*;
import org.springframework.mail.*;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

@Component
@RequiredArgsConstructor
public class BlogEmailSender {

    private final JavaMailSender mailSender;
    private final InfraProperties infraProperties;

    public void send(Email email) {
        SimpleMailMessage mailToSend = new SimpleMailMessage();
        mailToSend.setTo(email.getRecipients().toArray(new String[0]));
        mailToSend.setBcc(email.getBccs().toArray(new String[0]));
        mailToSend.setCc(email.getCcs().toArray(new String[0]));
        mailToSend.setSubject(email.getSubject());
        mailToSend.setText(email.getContent());
        mailToSend.setReplyTo(email.getReplyTo() == null || email.getReplyTo().isEmpty() ? infraProperties.getContactEmail() : email.getReplyTo());
        mailSender.send(mailToSend);
    }
}
