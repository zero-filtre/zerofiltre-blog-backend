package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.ITemplateEngine;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserForBroadcast;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

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
    UserProvider userProvider;

    private ZerofiltreEmailSender zerofiltreEmailSender;

    @BeforeEach
    void setUp() {
        javaMailSender = mock(JavaMailSenderImpl.class);
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));
        when(javaMailSender.createMimeMessage()).thenCallRealMethod();
        when(infraProperties.getContactEmail()).thenReturn(INFO_ZEROFILTRE_TECH);
        when(templateEngine.process(anyString(), any())).thenReturn(CONTENT);
        zerofiltreEmailSender = new ZerofiltreEmailSender(javaMailSender, infraProperties, templateEngine, userProvider);
    }

    @Test
    void mustSend_WithProperData() throws MessagingException {
        //ARRANGE
        Email email = new Email(Arrays.asList(ADDRESS, ANOTHER_ADDRESS), Arrays.asList(EMAIL_BLIND_COPY1, EMAIL_BLIND_COPY2), Arrays.asList(CERTIFIED_COPY1, CERTIFIED_COPY2),
                CONTENT, SUBJECT, REPLY_TO, Collections.emptyList(), Collections.emptyList());

        //ACT
        zerofiltreEmailSender.send(email, true);

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
        Email email = new Email(Arrays.asList(ADDRESS, ANOTHER_ADDRESS), Collections.emptyList(), Collections.emptyList(),
                CONTENT, SUBJECT, null, Collections.emptyList(), Collections.emptyList());

        //ACT
        zerofiltreEmailSender.send(email, true);

        //ASSERT
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender, times(1)).send(captor.capture());
        MimeMessage mail = captor.getValue();
        assertThat(((InternetAddress) mail.getReplyTo()[0]).getAddress()).isEqualTo(INFO_ZEROFILTRE_TECH);

    }

    @Test
    void mustSendToAllUsers_WithProperData() throws MessagingException {
        //ARRANGE
        Email email = new Email(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), CONTENT,
                SUBJECT, REPLY_TO, Collections.emptyList(), Collections.emptyList());

        UserForBroadcast userEmail1 = new UserForBroadcast(0, EMAIL_BLIND_COPY1, null, null, null);
        UserForBroadcast userEmail2 = new UserForBroadcast(0, EMAIL_BLIND_COPY2, null, null, null);
        UserForBroadcast userEmail3 = new UserForBroadcast(0, null, PAYMENT_EMAIL_BLIND_COPY, null, null);
        when(userProvider.allUsersForBroadcast()).thenReturn(Arrays.asList(userEmail1, userEmail2, userEmail3));

        //ACT
        zerofiltreEmailSender.sendForAllUsers(email);

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
    void mustSend42Emails_WithProperData() {
        //ARRANGE
        Email email = new Email(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), CONTENT,
                SUBJECT, REPLY_TO, Collections.emptyList(), Collections.emptyList());

        List<UserForBroadcast> list = new ArrayList<>();
        for (int i = 0; i < 42; i++) {
            String emailUser = "p" + i + "@email.com";
            UserForBroadcast userEmail = new UserForBroadcast(0, emailUser, null, null, null);
            list.add(userEmail);
        }

        when(userProvider.allUsersForBroadcast()).thenReturn(list);

        //ACT
        zerofiltreEmailSender.sendForAllUsers(email);

        //ASSERT
        verify(javaMailSender, times(3)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("When I want the list of emails from all users, then I get the list of emails from all users.")
    void shouldGetListEmailsFromAllUsers_whenListAllEmails() {
        //ARRANGE
        UserForBroadcast userEmail1 = new UserForBroadcast(0, EMAIL_BLIND_COPY1, null, null, null);
        UserForBroadcast userEmail2 = new UserForBroadcast(0, EMAIL_BLIND_COPY2, null, null, null);
        UserForBroadcast userEmail3 = new UserForBroadcast(0, null, PAYMENT_EMAIL_BLIND_COPY, null, null);
        UserForBroadcast userEmail4 = new UserForBroadcast(0, null, "bad.email_email.com", null, null);
        when(userProvider.allUsersForBroadcast()).thenReturn(Arrays.asList(userEmail1, userEmail2, userEmail3, userEmail4));

        //ACT
        Collection<List<String>> response = zerofiltreEmailSender.listAllEmails();

        //ASSERT
        verify(userProvider).allUsersForBroadcast();
        Collection<List<String>> collection = new ArrayList<>();
        collection.add(List.of(EMAIL_BLIND_COPY1, EMAIL_BLIND_COPY2, PAYMENT_EMAIL_BLIND_COPY));
        assertThat(response.containsAll(collection)).isTrue();
    }

}