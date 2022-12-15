package tech.zerofiltre.blog.infra.providers.notification.user;


import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.mail.*;
import org.springframework.mail.javamail.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class BlogEmailSenderTest {

    public static final String ADDRESS = "address";
    public static final String ANOTHER_ADDRESS = "another_address";
    public static final String SUBJECT = "subject";
    public static final String CONTENT = "content";
    public static final String INFO_ZEROFILTRE_TECH = "info@zerofiltre.tech";
    public static final String REPLY_TO = "info@zerofiltre.tech";
    @MockBean
    JavaMailSender javaMailSender;
    @MockBean
    InfraProperties infraProperties;
    private BlogEmailSender blogEmailSender;

    @BeforeEach
    void setUp() {
        blogEmailSender = new BlogEmailSender(javaMailSender, infraProperties);
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));
        when(infraProperties.getContactEmail()).thenReturn(INFO_ZEROFILTRE_TECH);
    }

    @Test
    void mustSend_WithProperData() {
        //ARRANGE
        Email email = new Email(Arrays.asList(ADDRESS, ANOTHER_ADDRESS), Collections.emptyList(), Collections.emptyList(), CONTENT, SUBJECT, REPLY_TO);

        //ACT
        blogEmailSender.send(email);

        //ASSERT
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(captor.capture());
        SimpleMailMessage mail = captor.getValue();
        assertThat(mail.getSubject()).isEqualTo(SUBJECT);
        assertThat(mail.getTo()).isNotNull();
        assertThat(Arrays.asList(mail.getTo())).contains(ADDRESS);
        assertThat(Arrays.asList(mail.getTo())).contains(ANOTHER_ADDRESS);
        assertThat(mail.getText()).isEqualTo(CONTENT);
        assertThat(mail.getReplyTo()).isEqualTo(INFO_ZEROFILTRE_TECH);

    }

    @Test
    void mustSend_WithDefault_replyTo() {
        //ARRANGE
        Email email = new Email(Arrays.asList(ADDRESS, ANOTHER_ADDRESS), Collections.emptyList(), Collections.emptyList(), CONTENT, SUBJECT, null);

        //ACT
        blogEmailSender.send(email);

        //ASSERT
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(captor.capture());
        SimpleMailMessage mail = captor.getValue();
        assertThat(mail.getReplyTo()).isEqualTo(INFO_ZEROFILTRE_TECH);

    }
}