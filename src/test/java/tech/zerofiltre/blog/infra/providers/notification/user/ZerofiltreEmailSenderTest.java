package tech.zerofiltre.blog.infra.providers.notification.user;


import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.core.io.*;
import org.springframework.mail.*;
import org.springframework.mail.javamail.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ZerofiltreEmailSenderTest {

    public static final String ADDRESS = "address";
    public static final String ANOTHER_ADDRESS = "another_address";
    public static final String SUBJECT = "subject";
    public static final String CONTENT = "content";
    public static final String INFO_ZEROFILTRE_TECH = "info@zerofiltre.tech";
    public static final String REPLY_TO = "info@zerofiltre.tech";
    JavaMailSender javaMailSender;
    @MockBean
    InfraProperties infraProperties;

    private ZerofiltreEmailSender zerofiltreEmailSender;

    @BeforeEach
    void setUp() {
        javaMailSender = mock(JavaMailSenderImpl.class);
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));
        when(javaMailSender.createMimeMessage()).thenCallRealMethod();
        when(infraProperties.getContactEmail()).thenReturn(INFO_ZEROFILTRE_TECH);
        zerofiltreEmailSender = new ZerofiltreEmailSender(javaMailSender, infraProperties);
    }

    @Test
    void mustSend_WithProperData() throws MessagingException, IOException {
        //ARRANGE
        Email email = new Email(Arrays.asList(ADDRESS, ANOTHER_ADDRESS), Collections.emptyList(), Collections.emptyList(), CONTENT, SUBJECT, REPLY_TO);

        //ACT
        zerofiltreEmailSender.send(email);

        //ASSERT
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender, times(1)).send(captor.capture());
        MimeMessage mail = captor.getValue();
        assertThat(mail.getSubject()).isEqualTo(SUBJECT);
        assertThat(mail.getAllRecipients().length).isEqualTo(2);
        assertThat(((InternetAddress)mail.getReplyTo()[0]).getAddress()).isEqualTo(INFO_ZEROFILTRE_TECH);

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
        assertThat(((InternetAddress)mail.getReplyTo()[0]).getAddress()).isEqualTo(INFO_ZEROFILTRE_TECH);

    }
}