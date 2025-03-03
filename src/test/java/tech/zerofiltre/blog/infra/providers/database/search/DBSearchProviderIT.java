package tech.zerofiltre.blog.infra.providers.database.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.search.model.SearchResult;
import tech.zerofiltre.blog.domain.search.model.UserSearchResult;
import tech.zerofiltre.blog.infra.providers.database.article.ArticleJPARepository;
import tech.zerofiltre.blog.infra.providers.database.article.model.ArticleJPA;
import tech.zerofiltre.blog.infra.providers.database.course.ChapterJPARepository;
import tech.zerofiltre.blog.infra.providers.database.course.CourseJPARepository;
import tech.zerofiltre.blog.infra.providers.database.course.LessonJPARepository;
import tech.zerofiltre.blog.infra.providers.database.course.model.ChapterJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.CourseJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.LessonJPA;
import tech.zerofiltre.blog.infra.providers.database.search.mapper.SearchResultJpaMapper;
import tech.zerofiltre.blog.infra.providers.database.user.UserJPARepository;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class DBSearchProviderIT {

    private final SearchResultJpaMapper mapper = new SearchResultJpaMapper();
    private ArticleJPA article1;
    private CourseJPA course2;
    private CourseJPA course1;
    private UserJPA user;
    private UserJPA anotherUser;
    @Autowired
    private ArticleJPARepository articleJPARepository;
    @Autowired
    private CourseJPARepository courseJPARepository;
    @Autowired
    private LessonJPARepository lessonJPARepository;
    @Autowired
    private ChapterJPARepository chapterJPARepository;
    private DBSearchProvider dbSearchProvider;
    @Autowired
    private UserJPARepository userJPARepository;

    @BeforeEach
    void setUp() {
        dbSearchProvider = new DBSearchProvider(articleJPARepository, courseJPARepository, lessonJPARepository, userJPARepository, mapper);

        // Set up test data
        article1 = new ArticleJPA();
        article1.setTitle("Spring Boot Guide");
        article1.setContent("Content about JAva");
        article1.setSummary("Summary of Boot Guide");
        article1.setStatus(Status.PUBLISHED);
        articleJPARepository.save(article1);

        course1 = new CourseJPA();
        course1.setTitle("Java Course");
        course1.setSubTitle("Introduction to Spring");
        course1.setSummary("Summary of Java Course");
        course1.setStatus(Status.PUBLISHED);
        courseJPARepository.save(course1);

        course2 = new CourseJPA();
        course2.setTitle("2nd Java Course");
        course2.setSubTitle("2nd Introduction to Spring");
        course2.setSummary("2nd Summary of Java Course");
        course2.setStatus(Status.PUBLISHED);
        courseJPARepository.save(course2);

        user = new UserJPA();
        user.setFullName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPaymentEmail("john.doe@examplepay.com");
        user.setBio(" I am John Doe");
        user.setProfession("Java Profession");
        user.setProfilePicture("picture");
        userJPARepository.save(user);

        anotherUser = new UserJPA();
        anotherUser.setFullName("Maw Well");
        anotherUser.setEmail("mw.w@example.com");
        anotherUser.setPaymentEmail("mw.w@examplepay.com");
        anotherUser.setBio(" I am Maw Well");
        anotherUser.setProfession("Java Profession");
        userJPARepository.save(anotherUser);

        ChapterJPA chapterJPA = new ChapterJPA();
        chapterJPA.setCourse(course1);
        chapterJPA = chapterJPARepository.save(chapterJPA);

        ChapterJPA chapterJPA2 = new ChapterJPA();
        chapterJPA2.setCourse(course1);
        chapterJPA2 = chapterJPARepository.save(chapterJPA2);

        LessonJPA lesson1 = new LessonJPA();
        lesson1.setTitle("Data JPA Lesson");
        lesson1.setContent("Content about Data JPA");
        lesson1.setSummary("Summary of Spring Data JPA Lesson");
        lesson1.setChapter(chapterJPA);

        LessonJPA lesson2 = new LessonJPA();
        lesson2.setTitle("Data JPA Lesson");
        lesson2.setContent("2nd Content about Data JPA");
        lesson2.setSummary("Summary of Spring Data JPA Lesson");
        lesson2.setChapter(chapterJPA2);


        lessonJPARepository.save(lesson1);
        lessonJPARepository.save(lesson2);
    }

    @Test
    void searchWorksProperly() {
        // Execute the search
        SearchResult result = dbSearchProvider.search("SPRING");

        // Verify the results
        assertThat(result.getArticles()).hasSize(1);
        assertThat(result.getArticles().get(0).getTitle()).isEqualTo("Spring Boot Guide");

        assertThat(result.getCourses()).hasSize(2);
        assertThat(result.getCourses().get(0).getTitle()).isEqualTo("Java Course");

        assertThat(result.getLessons()).hasSize(2);
        assertThat(result.getLessons().get(0).getTitle()).isEqualTo("Data JPA Lesson");
        assertThat(result.getLessons().get(0).getCourseId()).isEqualTo(course1.getId());

        assertThat(result.getLessons().get(1).getContent()).isEqualTo("2nd Content about Data JPA");
        assertThat(result.getLessons().get(1).getCourseId()).isEqualTo(course1.getId());
    }


    @Test
    void searchUsersWorksProperly() {
        // Execute the search
        List<UserSearchResult> result = dbSearchProvider.searchUsers("doe");

        // Verify the results
        assertThat(result).hasSize(1);
        UserSearchResult searchResult = result.get(0);
        assertThat(searchResult.getFullName()).isEqualTo(user.getFullName());
        assertThat(searchResult.getId()).isNotZero();
        assertThat(searchResult.getProfilePicture()).isEqualTo(user.getProfilePicture());

    }

    @Test
    void search_doesNotReturn_nonPublishedItems() {

        article1.setStatus(Status.DRAFT);
        articleJPARepository.save(article1);

        course1.setStatus(Status.DRAFT);
        course2.setStatus(Status.DRAFT);
        courseJPARepository.save(course1);
        courseJPARepository.save(course2);


        // Execute the search
        SearchResult result = dbSearchProvider.search("Spring");

        // Verify the results
        assertThat(result.getArticles()).isNull();

        assertThat(result.getCourses()).isNull();

        assertThat(result.getLessons()).isNull();

    }
}