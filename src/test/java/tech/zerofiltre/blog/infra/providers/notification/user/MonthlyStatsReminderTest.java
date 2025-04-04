package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.ITemplateEngine;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ArticleViewProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class MonthlyStatsReminderTest {

    public static final String SUBJECT = "Subject";
    public static final String CONTENT = "content";

    @MockBean
    ZerofiltreEmailSender emailSender;

    @MockBean
    InfraProperties infraProperties;

    @MockBean
    ITemplateEngine templateEngine;

    @MockBean
    MessageSource messages;

    @MockBean
    UserProvider userProvider;

    @MockBean
    ArticleProvider articleProvider;

    @MockBean
    ArticleViewProvider articleViewProvider;

    private MonthlyStatsReminder monthlyStatsReminder;

    @BeforeEach
    void setUp() {
        when(templateEngine.process(anyString(), any())).thenReturn(CONTENT);
        when(infraProperties.getEnv()).thenReturn("dev");

        monthlyStatsReminder = new MonthlyStatsReminder(userProvider, emailSender, messages, infraProperties, templateEngine, articleViewProvider, articleProvider);
    }

    @Test
    @DisplayName("When I want to send statistics to users who have subscribed to receive emails, then I send the emails")
    void shouldSendNewsletter_whenGivingEmailWithListOfRecipients() throws InterruptedException {
        //ARRANGE
        User user1 = new User();
        user1.setEmail("u1@a.a");
        user1.setLanguage("fr");

        User user2 = new User();
        user2.setEmail("u2@a.a");
        user2.setLanguage("en");

        User user3 = new User();
        user3.setEmail("u3@a.a");
        user3.setLanguage("fr");

        User user4 = new User();
        user4.setEmail("u3a.a");
        user4.setLanguage("fr");

        when(userProvider.users()).thenReturn(List.of(user1, user2, user3, user4));
        when(messages.getMessage(eq("message.stats.subject.remind"), any(), any())).thenReturn(SUBJECT);
        when(articleViewProvider.countArticlesReadByDatesAndUser(any(LocalDate.class), any(LocalDate.class), anyLong())).thenReturn(0).thenReturn(0).thenReturn(0);
        when(articleProvider.countPublishedArticlesByDatesAndUser(any(LocalDate.class), any(LocalDate.class), anyLong())).thenReturn(0).thenReturn(0).thenReturn(0);

        //ACT
        monthlyStatsReminder.sendStats();

        //ASSERT
        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        verify(emailSender, times(3)).send(captor.capture(), eq(true));
        List<Email> emails = captor.getAllValues();
        assertThat(emails.size()).isEqualTo(3);

        assertThat(emails.get(0).getRecipients().get(0)).isEqualTo(user1.getEmail());
        assertThat(emails.get(0).getSubject()).isEqualTo(SUBJECT);
        assertThat(emails.get(0).getBccs().size()).isEqualTo(0);
        assertThat(emails.get(0).getCcs().size()).isEqualTo(0);

        assertThat(emails.get(1).getRecipients().get(0)).isEqualTo(user2.getEmail());
        assertThat(emails.get(1).getSubject()).isEqualTo(SUBJECT);
        assertThat(emails.get(1).getBccs().size()).isEqualTo(0);
        assertThat(emails.get(1).getCcs().size()).isEqualTo(0);

        assertThat(emails.get(2).getRecipients().get(0)).isEqualTo(user3.getEmail());
        assertThat(emails.get(2).getSubject()).isEqualTo(SUBJECT);
        assertThat(emails.get(2).getBccs().size()).isEqualTo(0);
        assertThat(emails.get(2).getCcs().size()).isEqualTo(0);
    }

}