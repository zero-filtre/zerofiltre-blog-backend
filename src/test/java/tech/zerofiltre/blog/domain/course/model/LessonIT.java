package tech.zerofiltre.blog.domain.course.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.use_cases.course.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.*;
import tech.zerofiltre.blog.infra.providers.database.course.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.infra.providers.logging.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;

@DataJpaTest
@Import({DBCourseProvider.class, DBUserProvider.class, DBChapterProvider.class, DBLessonProvider.class, Slf4jLoggerProvider.class, DBTagProvider.class})
class LessonIT {

    public static final String TITLE = "Lesson 1";
    public static final String UPDATED_TITLE = "updated title";
    public static final String VIDEO = "https://www.youtube.com/watch?v=QH2-TGUlwu4";
    public static final String UPDATED_TYPE = "updated type";
    public static final String UPDATED_CONTENT = "updated content";
    private static final String UPDATED_SUMMARY = "updated summary";
    public static final String THUMBNAIL = "https://img.bb.com/THU";
    public static final String TITLE_2 = "title 2";

    @Autowired
    private LessonProvider lessonProvider;
    @Autowired
    private ChapterProvider chapterProvider;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private CourseProvider courseProvider;
    @Autowired
    private LoggerProvider loggerProvider;
    @Autowired
    private TagProvider tagProvider;

    private Chapter chapter;
    private User author;
    private Course course;
    private Lesson lesson;


    @Test
    void init_lesson_is_OK() throws ForbiddenActionException, ResourceNotFoundException {

        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        course = ZerofiltreUtils.createMockCourse(false, DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        lesson = Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init(TITLE, chapter.getId(), author.getId());

        org.assertj.core.api.Assertions.assertThat(lesson).isNotNull();
        org.assertj.core.api.Assertions.assertThat(lesson.getId()).isNotZero();
        org.assertj.core.api.Assertions.assertThat(lesson.getTitle()).isEqualTo(TITLE);
        org.assertj.core.api.Assertions.assertThat(lesson.getChapterId()).isEqualTo(course.getId());
        Assertions.assertThat(chapter.getLessons()).isEmpty();

    }

    @Test
    void save_lesson_is_OK() throws ForbiddenActionException, ResourceNotFoundException {

        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        course = ZerofiltreUtils.createMockCourse(false, DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        lesson = Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init(TITLE, chapter.getId(), author.getId());

        Lesson updatedLesson = Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .id(lesson.getId())
                .title(UPDATED_TITLE)
                .video(VIDEO)
                .content(UPDATED_CONTENT)
                .summary(UPDATED_SUMMARY)
                .thumbnail(THUMBNAIL)
                .resources(Collections.singletonList(Resource.builder().name("name").build()))
                .free(true)
                .chapterId(chapter.getId())
                .build()
                .save(author.getId());

        org.assertj.core.api.Assertions.assertThat(updatedLesson).isNotNull();
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getId()).isNotZero();
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getTitle()).isEqualTo(UPDATED_TITLE);
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getContent()).isEqualTo(UPDATED_CONTENT);
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getSummary()).isEqualTo(UPDATED_SUMMARY);
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getThumbnail()).isEqualTo(THUMBNAIL);
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getType()).isEqualTo("video");
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getVideo()).isEqualTo(VIDEO);
        org.assertj.core.api.Assertions.assertThat(updatedLesson.isFree()).isTrue();
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getChapterId()).isEqualTo(chapter.getId());
        Assertions.assertThat(lesson.getResources()).isEmpty();

    }

    @Test
    void delete_lesson_isOk() throws ForbiddenActionException, ResourceNotFoundException {
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        course = ZerofiltreUtils.createMockCourse(false, DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        lesson = Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init(TITLE, chapter.getId(), author.getId());

        assertThat(lessonProvider.lessonOfId(lesson.getId())).isNotEmpty();


        lesson.delete(author.getId());

        assertThat(lessonProvider.lessonOfId(lesson.getId())).isEmpty();

    }

    @Test
    @Disabled("Not working yet")
    void save_Lesson_increases_course_lessonsCount() throws ForbiddenActionException, ResourceNotFoundException {
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        Course course = courseService.init("A course", author);

        chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        Chapter chapter2 = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);
        chapter2 = chapterProvider.save(chapter2);

        Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init(TITLE, chapter.getId(), author.getId());

        Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init(TITLE_2, chapter2.getId(), author.getId());

        assertThat(courseService.getLessonsCount(course.getId())).isEqualTo(2);
    }
}
