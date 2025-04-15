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
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserEmailLanguage;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class MonthlyNewsletterReminderTest {

    public static final String SUBJECT = "Subject";
    public static final String CONTENT = "content";
    public static final String EMAIL_BLIND_COPY1 = "blindcopy1@email.com";
    public static final String EMAIL_BLIND_COPY2 = "blindcopy2@email.com";
    public static final String PAYMENT_EMAIL_BLIND_COPY = "paymentEmailblindcopy2@email.com";

    @MockBean
    ZerofiltreEmailSender zerofiltreEmailSender;

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
    CourseProvider courseProvider;

    private MonthlyNewsletterReminder monthlyNewsletterReminder;

    @BeforeEach
    void setUp() {
        when(templateEngine.process(anyString(), any())).thenReturn(CONTENT);
        monthlyNewsletterReminder = new MonthlyNewsletterReminder(zerofiltreEmailSender, infraProperties, templateEngine, messages, userProvider, articleProvider, courseProvider);
    }

    @Test
    void mustSendNewsletter_WithProperData() {
        //ARRANGE
        UserEmailLanguage userEmail1 = new UserEmailLanguage(EMAIL_BLIND_COPY1, null, "fr");
        UserEmailLanguage userEmail2 = new UserEmailLanguage(EMAIL_BLIND_COPY2, null, "");
        UserEmailLanguage userEmail3 = new UserEmailLanguage(null, PAYMENT_EMAIL_BLIND_COPY, null);
        UserEmailLanguage userEmail4 = new UserEmailLanguage(null, null, null);
        when(articleProvider.newArticlesFromLastMonth()).thenReturn(List.of(new Article()));
        when(courseProvider.newCoursesFromLastMonth()).thenReturn(List.of(new Course()));
        when(userProvider.allEmailsForBroadcast()).thenReturn(Arrays.asList(userEmail1, userEmail2, userEmail3, userEmail4));
        when(messages.getMessage(eq("remind_newsletter_message"), any(), any())).thenReturn(SUBJECT);

        //ACT
        monthlyNewsletterReminder.sendNewsletter();

        //ASSERT
        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        verify(zerofiltreEmailSender, times(3)).send(captor.capture(), eq(true));
        List<Email> emails = captor.getAllValues();
        assertThat(emails.size()).isEqualTo(3);

        assertThat(emails.get(0).getRecipients().get(0)).isEqualTo(userEmail1.getEmail());
        assertThat(emails.get(0).getSubject()).isEqualTo(SUBJECT);
        assertThat(emails.get(0).getBccs().size()).isEqualTo(0);
        assertThat(emails.get(0).getCcs().size()).isEqualTo(0);

        assertThat(emails.get(1).getRecipients().get(0)).isEqualTo(userEmail2.getEmail());
        assertThat(emails.get(1).getSubject()).isEqualTo(SUBJECT);
        assertThat(emails.get(1).getBccs().size()).isEqualTo(0);
        assertThat(emails.get(1).getCcs().size()).isEqualTo(0);

        assertThat(emails.get(2).getRecipients().get(0)).isEqualTo(userEmail3.getPaymentEmail());
        assertThat(emails.get(2).getSubject()).isEqualTo(SUBJECT);
        assertThat(emails.get(2).getBccs().size()).isEqualTo(0);
        assertThat(emails.get(2).getCcs().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("When I give an email with a list of recipients then I send the newsletter")
    void shouldSendNewsletter_whenGivingEmailWithListOfRecipients() {
        //ARRANGE
        List<String> emailList = List.of(EMAIL_BLIND_COPY1, EMAIL_BLIND_COPY2, PAYMENT_EMAIL_BLIND_COPY);
        Email email = new Email();
        email.setRecipients(emailList);
        when(articleProvider.newArticlesFromLastMonth()).thenReturn(List.of(new Article()));
        when(courseProvider.newCoursesFromLastMonth()).thenReturn(List.of(new Course()));
        when(messages.getMessage(eq("remind_newsletter_message"), any(), any())).thenReturn(SUBJECT);

        //ACT
        monthlyNewsletterReminder.setEmailTest(email);
        monthlyNewsletterReminder.sendNewsletter();
        monthlyNewsletterReminder.setEmailTest(null);

        //ASSERT
        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        verify(zerofiltreEmailSender, times(3)).send(captor.capture(), eq(true));
        List<Email> emails = captor.getAllValues();
        assertThat(emails.size()).isEqualTo(3);

        assertThat(emails.get(0).getRecipients().get(0)).isEqualTo(emailList.get(0));
        assertThat(emails.get(0).getSubject()).isEqualTo(SUBJECT);
        assertThat(emails.get(0).getBccs().size()).isEqualTo(0);
        assertThat(emails.get(0).getCcs().size()).isEqualTo(0);

        assertThat(emails.get(1).getRecipients().get(0)).isEqualTo(emailList.get(1));
        assertThat(emails.get(1).getSubject()).isEqualTo(SUBJECT);
        assertThat(emails.get(1).getBccs().size()).isEqualTo(0);
        assertThat(emails.get(1).getCcs().size()).isEqualTo(0);

        assertThat(emails.get(2).getRecipients().get(0)).isEqualTo(emailList.get(2));
        assertThat(emails.get(2).getSubject()).isEqualTo(SUBJECT);
        assertThat(emails.get(2).getBccs().size()).isEqualTo(0);
        assertThat(emails.get(2).getCcs().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("When there are no new articles or courses in the previous month, no newsletter is sent")
    void shouldNotSendNewsletter_whenNotArticleOrCourse_inPreviousMonth() {
        //ARRANGE
        List<String> emailList = List.of(EMAIL_BLIND_COPY1, EMAIL_BLIND_COPY2, PAYMENT_EMAIL_BLIND_COPY);
        Email email = new Email();
        email.setRecipients(emailList);
        when(messages.getMessage(eq("remind_newsletter_message"), any(), any())).thenReturn(SUBJECT);

        //ACT
        monthlyNewsletterReminder.setEmailTest(email);
        monthlyNewsletterReminder.sendNewsletter();
        monthlyNewsletterReminder.setEmailTest(null);

        //ASSERT
        verify(zerofiltreEmailSender, never()).send(any(Email.class), anyBoolean());
    }

    @Test
    @DisplayName("When I'm looking for new articles from last month, I return the list")
    void shouldReturnList_whenSearchingNewArticlesFromLastMonth() {
        //GIVEN
        Article article1 = new Article();
        article1.setId(1);
        Article article2 = new Article();
        article2.setId(2);
        Article article3 = new Article();
        article3.setId(3);

        List<Article> articleList = List.of(article1, article2, article3);

        when(articleProvider.newArticlesFromLastMonth()).thenReturn(articleList);

        //WHEN
        List<Article> response = monthlyNewsletterReminder.findNewArticles();

        //THEN
        verify(articleProvider).newArticlesFromLastMonth();
        assertThat(response).isEqualTo(articleList);
    }

    @Test
    @DisplayName("When I'm looking for new courses from last month, I return the list")
    void shouldReturnList_whenSearchingNewCoursesFromLastMonth() {
        //GIVEN
        Course course1 = new Course();
        course1.setId(1);
        Course course2 = new Course();
        course2.setId(2);
        Course course3 = new Course();
        course3.setId(3);

        List<Course> courseList = List.of(course1, course2, course3);

        when(courseProvider.newCoursesFromLastMonth()).thenReturn(courseList);

        //WHEN
        List<Course> response = monthlyNewsletterReminder.findNewCourses();

        //THEN
        verify(courseProvider).newCoursesFromLastMonth();
        assertThat(response).isEqualTo(courseList);
    }

}