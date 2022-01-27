package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.*;
import org.springframework.core.env.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ConfirmRegistrationReminderTest {

    public static final String EMAIL_1 = "email1";
    public static final String EMAIL_2 = "email2";
    public static final String EMAIL_3 = "email3";
    public static final String SUBJECT = "subject";
    public static final String CONTENT = "content";
    private ConfirmRegistrationReminder confirmRegistrationReminder;

    @MockBean
    UserProvider userProvider;

    @MockBean
    Environment environment;

    @MockBean
    BlogEmailSender blogEmailSender;

    @MockBean
    MessageSource messageSource;

    @MockBean
    VerificationTokenManager verificationTokenManager;


    @BeforeEach
    void setUp() {
        confirmRegistrationReminder = new ConfirmRegistrationReminder(
                userProvider,
                blogEmailSender,
                messageSource,
                verificationTokenManager,
                environment
        );
    }

    @Test
    void remindConfirmRegistration_mustSendEmailWithProperData_ForUAT() {
        //ARRANGE
        testRemindConfirmRegistration("uat", "https://blog-uat.zerofiltre.tech");

    }

    @Test
    void remindConfirmRegistration_mustSendEmailWithProperData_ForProd() {
        testRemindConfirmRegistration("prod", "https://blog.zerofiltre.tech");

    }

    @Test
    void remindConfirmRegistration_mustSendEmailWithProperData_ForDev() {
        testRemindConfirmRegistration("dev", "https://blog-dev.zerofiltre.tech");

    }

    @Test
    void remindConfirmRegistration_mustSendEmailWithProperData() {
        testRemindConfirmRegistration("", "https://blog-dev.zerofiltre.tech");

    }

    private void testRemindConfirmRegistration(String profile, String frontAppURL) {
        //ARRANGE
        User user = new User();
        user.setEmail(EMAIL_1);
        user.setLanguage("FR");
        user.setLastName("lastname1");
        user.setFirstName("firstname1");

        User user1 = new User();
        user1.setEmail(EMAIL_2);
        user1.setLanguage("DE");
        user1.setLastName("lastname2");
        user1.setFirstName("firstname2");

        User user2 = new User();
        user2.setEmail(EMAIL_3);
        user2.setLanguage("EN");
        user2.setLastName("lastname3");
        user2.setFirstName("firstname3");

        List<User> users = Arrays.asList(user, user1, user2);

        when(environment.getActiveProfiles()).thenReturn(new String[]{profile});
        when(userProvider.nonActiveUsers()).thenReturn(users);
        when(messageSource.getMessage(eq("message.registration.subject.remind"), any(), any())).thenReturn(SUBJECT);
        when(messageSource.getMessage(eq("message.registration.success.remind.content"), any(), any())).thenReturn(CONTENT);
        when(messageSource.getMessage(eq("message.greetings"), any(), any())).thenReturn("greetings");


        //ACT
        confirmRegistrationReminder.remindConfirmRegistration();


        //ASSERT
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);

        verify(blogEmailSender, times(3)).send(emailCaptor.capture(), subjectCaptor.capture(), contentCaptor.capture());
        List<String> capturedEmailList = emailCaptor.getAllValues();
        List<String> capturedSubjectList = subjectCaptor.getAllValues();
        List<String> capturedContentList = contentCaptor.getAllValues();

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


}