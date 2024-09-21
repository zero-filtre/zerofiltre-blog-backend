package tech.zerofiltre.blog.domain.course.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.course.features.course.CourseService;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.article.DBTagProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBChapterProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBLessonProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.logging.Slf4jLoggerProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static tech.zerofiltre.blog.domain.article.model.Status.DRAFT;

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
        org.assertj.core.api.Assertions.assertThat(lesson.getChapterId()).isEqualTo(chapter.getId());
        org.assertj.core.api.Assertions.assertThat(lesson.getNumber()).isEqualTo(1);

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

        org.assertj.core.api.Assertions.assertThat(lesson.getNumber()).isEqualTo(1);

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
                .number(40)
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
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getNumber()).isEqualTo(1);
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
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_Lesson_increases_course_lessonsCount() throws ForbiddenActionException, ResourceNotFoundException {
        author = ZerofiltreUtils.createMockUser(false);
        author.setEmail("test@mail.uk");
        author.setPseudoName("pseudo");
        author = userProvider.save(author);


        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
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
        course = courseProvider.courseOfId(course.getId()).get();

        assertThat(course.getLessonsCount()).isEqualTo(2);
    }
}
