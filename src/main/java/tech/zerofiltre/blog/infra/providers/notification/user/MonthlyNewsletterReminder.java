package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserForBroadcast;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.*;

import static tech.zerofiltre.blog.util.ZerofiltreUtils.getOriginUrl;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyNewsletterReminder {

    private final ZerofiltreEmailSender emailSender;
    private final InfraProperties infraProperties;
    private final ITemplateEngine emailTemplateEngine;
    private final MessageSource messages;
    private final UserProvider userProvider;
    private final ArticleProvider articleProvider;
    private final CourseProvider courseProvider;

    @Setter
    private Email emailTest = null;

    @Scheduled(cron = "${zerofiltre.infra.newsletter.reminder.cron}")
    public void sendNewsletter() {
        List<Article> newArticlesList = findNewArticles();
        List<Course> newCoursesList = findNewCourses();

        if(newArticlesList.isEmpty() && newCoursesList.isEmpty()) return;

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("newArticles", newArticlesList);
        templateModel.put("newCourses", newCoursesList);
        templateModel.put("originUrl", getOriginUrl(infraProperties.getEnv()));
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);

        List<UserForBroadcast> listAllUsers = new ArrayList<>();

        if(emailTest != null) {
            for(String recipient : emailTest.getRecipients()) {
                listAllUsers.add(new UserForBroadcast(0, recipient, null, "fr", null));
            }
        } else {
            listAllUsers = userProvider.allUsersForBroadcast();
        }

        for(UserForBroadcast u : listAllUsers) {
            Optional<String> mail = ZerofiltreUtils.getValidEmailForBroadcast(u);
            if(mail.isEmpty()) continue;

            String language = (u.getLanguage() != null && !u.getLanguage().isBlank()) ? u.getLanguage() : Locale.FRANCE.getLanguage();
            Locale locale = new Locale(language);
            String subject = messages.getMessage("remind_newsletter_message", null, locale);
            thymeleafContext.setLocale(locale);

            String emailContent = emailTemplateEngine.process("newsletter.html", thymeleafContext);
            log.info("email content {}", emailContent);

            Email email = new Email();
            email.setReplyTo(infraProperties.getContactEmail());
            email.setRecipients(Collections.singletonList(mail.get()));
            email.setSubject(subject);
            email.setContent(emailContent);
            emailSender.send(email, true);
        }
    }

    List<Article> findNewArticles() {
        return articleProvider.newArticlesFromLastMonth();
    }

    List<Course> findNewCourses() {
        return courseProvider.newCoursesFromLastMonth();
    }

}
