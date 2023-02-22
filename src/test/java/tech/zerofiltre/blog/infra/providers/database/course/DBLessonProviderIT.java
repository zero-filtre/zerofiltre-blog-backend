package tech.zerofiltre.blog.infra.providers.database.course;

import org.assertj.core.api.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
@Import({DBChapterProvider.class, DBCourseProvider.class, DBUserProvider.class, DBSubscriptionProvider.class})
class DBLessonProviderIT {

    DBLessonProvider lessonProvider;

    @Autowired
    LessonJPARepository lessonJPARepository;

    @Autowired
    LessonJPANumberRepository lessonJPANumberRepository;

    @Autowired
    SubscriptionJPARepository subscriptionJPARepository;

    @Autowired
    DBUserProvider dbUserProvider;

    @Autowired
    DBCourseProvider dbCourseProvider;

    @Autowired
    DBChapterProvider dbChapterProvider;

    @Autowired
    DBSubscriptionProvider dbSubscriptionProvider;

    @BeforeEach
    void setUp() {
        lessonProvider = new DBLessonProvider(lessonJPARepository, lessonJPANumberRepository, subscriptionJPARepository);
    }

    @Test
    void save_creates_number_if_zero() {
        //given
        Lesson lesson = Lesson.builder()
                .title("title")
                .build();

        //when
        Lesson result = lessonProvider.save(lesson);

        //then
        assertThat(result.getNumber()).isNotZero();

    }

    @Test
    void delete_lesson_is_ok() {
        //given
        User author = ZerofiltreUtils.createMockUser(false);
        author = dbUserProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course = dbCourseProvider.save(course);

        Chapter chapter = ZerofiltreUtils.createMockChapter(false, dbChapterProvider, Collections.emptyList(), course.getId());
        chapter = dbChapterProvider.save(chapter);

        Lesson lesson = Lesson.builder()
                .title("title")
                .chapterId(chapter.getId())
                .build();
        lesson = lessonProvider.save(lesson);
        assertThat(lessonProvider.lessonOfId(lesson.getId())).isPresent();

        //when
        lessonProvider.delete(lesson);

        //then
        assertThat(lessonProvider.lessonOfId(lesson.getId())).isEmpty();
    }

    @Test
    void delete_lesson_removes_it_from_subscription_completedLessons() throws BlogException {
        //given

        User author = ZerofiltreUtils.createMockUser(false);
        author = dbUserProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course = dbCourseProvider.save(course);

        Chapter chapter = ZerofiltreUtils.createMockChapter(false, dbChapterProvider, Collections.emptyList(), course.getId());
        chapter = dbChapterProvider.save(chapter);

        Lesson lesson = Lesson.builder()
                .title("title")
                .content("content")
                .chapterId(chapter.getId())
                .build();

        lesson = lessonProvider.save(lesson);
        assertThat(lessonProvider.lessonOfId(lesson.getId())).isPresent();

        Subscription subscription = ZerofiltreUtils.createMockSubscription(false, author, course);
        dbSubscriptionProvider.save(subscription);

        //when
        lessonProvider.delete(lesson);

        //then
        assertThat(lessonProvider.lessonOfId(lesson.getId())).isEmpty();
        Optional<Subscription> updatedSubscription = dbSubscriptionProvider.subscriptionOf(author.getId(), course.getId(), true);
        assertThat(updatedSubscription).isPresent();
        AssertionsForClassTypes.assertThat(updatedSubscription.get().getCompletedLessons().size()).isEqualTo(0);
    }
}
