package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
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
    BlogEmailSender blogEmailSender;
    @MockBean
    MessageSource messageSource;
    @MockBean
    InfraProperties infraProperties;
    @MockBean
    VerificationTokenProvider tokenProvider;
    LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);
    private ConfirmRegistrationReminder confirmRegistrationReminder;

    @BeforeEach
    void setUp() {
        confirmRegistrationReminder = new ConfirmRegistrationReminder(
                userProvider,
                blogEmailSender,
                messageSource,
                tokenProvider,
                infraProperties
        );
        when(infraProperties.getEnv()).thenReturn("dev");
        when(tokenProvider.generate(any())).thenReturn(new VerificationToken(new User(), "TOKEN", expiryDate));
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
        verify(blogEmailSender, times(3)).send(emailCaptor.capture());
        List<String> capturedEmailList = new ArrayList<>();
        List<String> capturedSubjectList = new ArrayList<>();
        List<String> capturedContentList = new ArrayList<>();

        emailCaptor.getAllValues().forEach(value -> {
            capturedEmailList.addAll(value.getRecipients());
            capturedSubjectList.add(value.getSubject());
            capturedContentList.add(value.getContent());
        });

        assertThat(capturedEmailList.stream().anyMatch(
                s -> List.of(EMAIL_1, EMAIL_2, EMAIL_3).contains(s)
        )).isTrue();

        assertThat(capturedSubjectList.stream().anyMatch(
                s -> s.equals(SUBJECT)
        )).isTrue();

        assertThat(capturedContentList.stream().anyMatch(
                s -> s.contains(CONTENT) &&
                        s.contains("/accountConfirmation?token=") &&
                        s.contains(frontAppURL)
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

        verify(blogEmailSender, times(2)).send(emailCaptor.capture());
        List<String> capturedEmailList = new ArrayList<>();
        emailCaptor.getAllValues().forEach(value -> {
            capturedEmailList.addAll(value.getRecipients());
        });
        assertThat(capturedEmailList.stream().noneMatch(s -> s.equals(INVALID_EMAIL))).isTrue();

    }


}