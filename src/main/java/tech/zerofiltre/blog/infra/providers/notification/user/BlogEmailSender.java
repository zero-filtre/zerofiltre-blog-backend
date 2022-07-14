package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.*;
import org.springframework.mail.*;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.*;

@Component
@RequiredArgsConstructor
public class BlogEmailSender {

    private final JavaMailSender mailSender;

    public void send(String recipientAddress, String subject, String emailContent) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(emailContent);
        mailSender.send(email);
    }
}
