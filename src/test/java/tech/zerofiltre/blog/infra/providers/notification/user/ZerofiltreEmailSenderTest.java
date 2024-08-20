package tech.zerofiltre.blog.infra.providers.notification.user;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.ITemplateEngine;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserEmail;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ZerofiltreEmailSenderTest {

    public static final String ADDRESS = "address";
    public static final String ANOTHER_ADDRESS = "another_address";
    public static final String SUBJECT = "subject";
    public static final String CONTENT = "content";
    public static final String INFO_ZEROFILTRE_TECH = "info@zerofiltre.tech";
    public static final String REPLY_TO = "info@zerofiltre.tech";
    public static final String EMAIL_BLIND_COPY1 = "blindcopy1@email.com";
    public static final String EMAIL_BLIND_COPY2 = "blindcopy2@email.com";
    public static final String PAYMENT_EMAIL_BLIND_COPY = "paymentEmailblindcopy2@email.com";
    public static final String CERTIFIED_COPY1 = "blindcopy1@email.com";
    public static final String CERTIFIED_COPY2 = "blindcopy2@email.com";
    JavaMailSender javaMailSender;
    @MockBean
    InfraProperties infraProperties;

    @MockBean
    ITemplateEngine templateEngine;

    @MockBean
    DBUserProvider dbUserProvider;

    private ZerofiltreEmailSender zerofiltreEmailSender;

    @BeforeEach
    void setUp() {
        javaMailSender = mock(JavaMailSenderImpl.class);
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));
        when(javaMailSender.createMimeMessage()).thenCallRealMethod();
        when(infraProperties.getContactEmail()).thenReturn(INFO_ZEROFILTRE_TECH);
        zerofiltreEmailSender = new ZerofiltreEmailSender(javaMailSender, infraProperties, templateEngine, dbUserProvider);
    }

    @Test
    void mustSend_WithProperData() throws MessagingException, IOException {
        //ARRANGE
        Email email = new Email(Arrays.asList(ADDRESS, ANOTHER_ADDRESS), Arrays.asList(EMAIL_BLIND_COPY1, EMAIL_BLIND_COPY2), Arrays.asList(CERTIFIED_COPY1, CERTIFIED_COPY2), CONTENT, SUBJECT, REPLY_TO);

        //ACT
        zerofiltreEmailSender.send(email);

        //ASSERT
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender, times(1)).send(captor.capture());
        MimeMessage mail = captor.getValue();
        assertThat(mail.getSubject()).isEqualTo(SUBJECT);
        assertThat(mail.getAllRecipients().length).isEqualTo(6);
        assertThat(((InternetAddress) mail.getReplyTo()[0]).getAddress()).isEqualTo(INFO_ZEROFILTRE_TECH);

    }

    @Test
    void mustSend_WithDefault_replyTo() throws MessagingException {
        //ARRANGE
        Email email = new Email(Arrays.asList(ADDRESS, ANOTHER_ADDRESS), Collections.emptyList(), Collections.emptyList(), CONTENT, SUBJECT, null);

        //ACT
        zerofiltreEmailSender.send(email);

        //ASSERT
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender, times(1)).send(captor.capture());
        MimeMessage mail = captor.getValue();
        assertThat(((InternetAddress) mail.getReplyTo()[0]).getAddress()).isEqualTo(INFO_ZEROFILTRE_TECH);

    }

    @Test
    void mustSendToAllUsers_WithProperData() throws MessagingException, IOException {
        //ARRANGE
        Email email = new Email(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), CONTENT, SUBJECT, REPLY_TO);

        UserEmail userEmail1 = new UserEmail(EMAIL_BLIND_COPY1, null);
        UserEmail userEmail2 = new UserEmail(EMAIL_BLIND_COPY2, null);
        UserEmail userEmail3 = new UserEmail( null, PAYMENT_EMAIL_BLIND_COPY);
        when(dbUserProvider.allEmails()).thenReturn(Arrays.asList(userEmail1, userEmail2, userEmail3));

        //ACT
        zerofiltreEmailSender.sendForAllUsers(email, false);

        //ASSERT
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender, times(1)).send(captor.capture());
        MimeMessage mail = captor.getValue();
        assertThat(((InternetAddress) mail.getFrom()[0]).getAddress()).isEqualTo(INFO_ZEROFILTRE_TECH);
        assertThat(mail.getSubject()).isEqualTo(SUBJECT);
        assertThat(mail.getAllRecipients().length).isEqualTo(4);
        assertThat(((InternetAddress) mail.getAllRecipients()[0]).getAddress()).isEqualTo(INFO_ZEROFILTRE_TECH);
        assertThat(((InternetAddress) mail.getAllRecipients()[1]).getAddress()).isEqualTo(EMAIL_BLIND_COPY1);
        assertThat(((InternetAddress) mail.getAllRecipients()[2]).getAddress()).isEqualTo(EMAIL_BLIND_COPY2);
        assertThat(((InternetAddress) mail.getAllRecipients()[3]).getAddress()).isEqualTo(PAYMENT_EMAIL_BLIND_COPY);
        assertThat(((InternetAddress) mail.getReplyTo()[0]).getAddress()).isEqualTo(INFO_ZEROFILTRE_TECH);
    }

    @Test
    void mustSend42Emails_WithProperData() throws MessagingException, IOException {
        //ARRANGE
        Email email = new Email(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), CONTENT, SUBJECT, REPLY_TO);

        List<UserEmail> list = new ArrayList<>();
        for(int i = 0; i < 42; i++) {
            String emailUser = "p" + String.valueOf(i) + "@email.com";
            UserEmail userEmail = new UserEmail(emailUser, null);
            list.add(userEmail);
        }

        when(dbUserProvider.allEmails()).thenReturn(list);

        //ACT
        zerofiltreEmailSender.sendForAllUsers(email, false);

        //ASSERT
        verify(javaMailSender, times(3)).send(any(MimeMessage.class));
    }
}