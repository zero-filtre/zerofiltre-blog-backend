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
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserForBroadcast;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;

import java.time.LocalDateTime;
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
        UserForBroadcast user1 = new UserForBroadcast();
        user1.setId(1);
        user1.setEmail("u1@a.a");
        user1.setLanguage("fr");
        user1.setFullName("user1");

        UserForBroadcast user2 = new UserForBroadcast();
        user2.setId(2);
        user2.setEmail("u2@a.a");
        user2.setLanguage("en");
        user2.setFullName("user2");

        UserForBroadcast user3 = new UserForBroadcast();
        user3.setId(3);
        user3.setEmail("u3");
        user3.setPaymentEmail("u3@a.a");
        user3.setLanguage(null);
        user3.setFullName("user3");

        UserForBroadcast user4 = new UserForBroadcast();
        user4.setId(4);
        user4.setEmail("u4");
        user4.setPaymentEmail("u4a.a");
        user4.setLanguage("en");
        user4.setFullName("user4");

        when(userProvider.allUsersForBroadcast()).thenReturn(List.of(user1, user2, user3, user4));
        when(messages.getMessage(eq("message.stats.subject.remind"), any(), any())).thenReturn(SUBJECT);
        when(articleViewProvider.countArticlesReadByDatesAndUser(any(LocalDateTime.class), any(LocalDateTime.class), anyLong())).thenReturn(1).thenReturn(1).thenReturn(0);
        when(articleProvider.countPublishedArticlesByDatesAndUser(any(LocalDateTime.class), any(LocalDateTime.class), anyLong())).thenReturn(1).thenReturn(0).thenReturn(1);

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

        assertThat(emails.get(2).getRecipients().get(0)).isEqualTo(user3.getPaymentEmail());
        assertThat(emails.get(2).getSubject()).isEqualTo(SUBJECT);
        assertThat(emails.get(2).getBccs().size()).isEqualTo(0);
        assertThat(emails.get(2).getCcs().size()).isEqualTo(0);
    }

}