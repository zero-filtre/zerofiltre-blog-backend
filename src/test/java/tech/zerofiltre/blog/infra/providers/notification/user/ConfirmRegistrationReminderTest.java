package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.IContext;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.VerificationToken;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = "zerofiltre.key=dev")
class ConfirmRegistrationReminderTest {

    public static final String EMAIL_1 = "email1@gmail.com";
    public static final String EMAIL_2 = "email2@gmail.com";
    public static final String EMAIL_3 = "email3@gmail.com";
    public static final String INVALID_EMAIL = "email4";
    public static final String SUBJECT = "subject";
    public static final String CONTENT = "content";
    @MockBean
    UserProvider userProvider;
    @MockBean
    ZerofiltreEmailSender zerofiltreEmailSender;
    @MockBean
    MessageSource messageSource;
    @MockBean
    InfraProperties infraProperties;
    @MockBean
    VerificationTokenProvider tokenProvider;
    @Mock
    ITemplateEngine emailTemplateEngine;

    LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);
    private ConfirmRegistrationReminder confirmRegistrationReminder;

    @BeforeEach
    void setUp() {
        confirmRegistrationReminder = new ConfirmRegistrationReminder(
                userProvider,
                zerofiltreEmailSender,
                messageSource,
                tokenProvider,
                infraProperties,
                emailTemplateEngine
        );
        when(infraProperties.getEnv()).thenReturn("dev");
        when(tokenProvider.generate(any())).thenReturn(new VerificationToken(new User(), "TOKEN", expiryDate));
        when(emailTemplateEngine.process(any(String.class),any(IContext.class))).thenReturn("<a href=zerofiltre.tech>Home</a>");
    }

    @Test
    void remindConfirmRegistration_mustSendEmailWithProperData_ForUAT() {
        when(infraProperties.getEnv()).thenReturn("uat");
        testRemindConfirmRegistration("https://uat.zerofiltre.tech");

    }

    @Test
    void remindConfirmRegistration_mustSendEmailWithProperData_ForProd() {
        when(infraProperties.getEnv()).thenReturn("prod");
        testRemindConfirmRegistration("https://zerofiltre.tech");

    }

    @Test
    void remindConfirmRegistration_mustSendEmailWithProperData_ForDev() {
        testRemindConfirmRegistration("https://dev.zerofiltre.tech");

    }

    @Test
    void remindConfirmRegistration_mustSendEmailWithProperData() {
        testRemindConfirmRegistration("https://dev.zerofiltre.tech");

    }


    private void testRemindConfirmRegistration(String frontAppURL) {
        //ARRANGE
        User user = new User();
        user.setEmail(EMAIL_1);
        user.setLanguage("FR");
        user.setFullName("firstname1");

        User user1 = new User();
        user1.setEmail(EMAIL_2);
        user1.setLanguage("DE");
        user1.setFullName("firstname2");

        User user2 = new User();
        user2.setEmail(EMAIL_3);
        user2.setLanguage("EN");
        user2.setFullName("firstname3");

        List<User> users = Arrays.asList(user, user1, user2);

        when(userProvider.nonActiveUsers()).thenReturn(users);
        when(messageSource.getMessage(eq("message.registration.subject.remind"), any(), any())).thenReturn(SUBJECT);
        when(messageSource.getMessage(eq("message.registration.success.remind.content"), any(), any())).thenReturn(CONTENT);
        when(messageSource.getMessage(eq("message.greetings"), any(), any())).thenReturn("greetings");


        //ACT
        confirmRegistrationReminder.remindConfirmRegistration();


        //ASSERT
        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);

        Email email = new Email();
        verify(zerofiltreEmailSender, times(3)).send(emailCaptor.capture(), anyBoolean());
        List<String> capturedEmailList = new ArrayList<>();
        List<String> capturedSubjectList = new ArrayList<>();

        emailCaptor.getAllValues().forEach(value -> {
            capturedEmailList.addAll(value.getRecipients());
            capturedSubjectList.add(value.getSubject());
        });

        assertThat(capturedEmailList.stream().anyMatch(
                s -> List.of(EMAIL_1, EMAIL_2, EMAIL_3).contains(s)
        )).isTrue();

        assertThat(capturedSubjectList.stream().anyMatch(
                s -> s.equals(SUBJECT)
        )).isTrue();

    }

    @Test
    void remindConfirmRegistration_mustNotSendEmails_onInvalidEmails() {
        //ARRANGE
        User user = new User();
        user.setEmail(EMAIL_1);


        User user1 = new User();
        user1.setEmail(EMAIL_2);


        User user2 = new User();
        user2.setEmail(INVALID_EMAIL);

        List<User> users = Arrays.asList(user, user1, user2);

        when(userProvider.nonActiveUsers()).thenReturn(users);
        when(messageSource.getMessage(eq("message.registration.subject.remind"), any(), any())).thenReturn(SUBJECT);
        when(messageSource.getMessage(eq("message.registration.success.remind.content"), any(), any())).thenReturn(CONTENT);
        when(messageSource.getMessage(eq("message.greetings"), any(), any())).thenReturn("greetings");


        //ACT
        confirmRegistrationReminder.remindConfirmRegistration();


        //ASSERT
        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);

        verify(zerofiltreEmailSender, times(2)).send(emailCaptor.capture(), anyBoolean());
        List<String> capturedEmailList = new ArrayList<>();
        emailCaptor.getAllValues().forEach(value -> {
            capturedEmailList.addAll(value.getRecipients());
        });
        assertThat(capturedEmailList.stream().noneMatch(s -> s.equals(INVALID_EMAIL))).isTrue();

    }


}