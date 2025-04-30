package tech.zerofiltre.blog.domain.course.features.lesson;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.course.features.course.CourseService;
import tech.zerofiltre.blog.domain.course.model.Chapter;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Lesson;
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
import tech.zerofiltre.blog.util.DataChecker;
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.zerofiltre.blog.domain.article.model.Status.DRAFT;

@DataJpaTest
@Import({DBCourseProvider.class, DBUserProvider.class, DBChapterProvider.class, DBLessonProvider.class, Slf4jLoggerProvider.class, DBTagProvider.class})
public class LessonServiceIT {

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
    @MockBean
    private DataChecker checker;
    @MockBean
    private CompanyCourseProvider companyCourseProvider;
    @MockBean
    private EnrollmentProvider enrollmentProvider;

    private LessonService lessonService;

    private Chapter chapter;
    private User author;
    private Course course;
    private Lesson lesson;

    @BeforeEach
    void setup() {
        lessonService = new LessonService(lessonProvider, chapterProvider, userProvider, courseProvider, enrollmentProvider, checker);
    }

    @Test
    void init_lesson_is_OK() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        author = ZerofiltreUtilsTest.createMockUser(false);
        author = userProvider.save(author);

        course = ZerofiltreUtilsTest.createMockCourse(false, DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = ZerofiltreUtilsTest.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        //WHEN
        lesson = lessonService.init(TITLE, chapter.getId(), author.getId());

        //TEHN
        assertThat(lesson).isNotNull();
        assertThat(lesson.getId()).isNotZero();
        assertThat(lesson.getTitle()).isEqualTo(TITLE);
        assertThat(lesson.getChapterId()).isEqualTo(chapter.getId());
        assertThat(lesson.getNumber()).isEqualTo(1);

    }

    @Test
    void update_lesson_is_OK() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        author = ZerofiltreUtilsTest.createMockUser(false);
        author = userProvider.save(author);

        course = ZerofiltreUtilsTest.createMockCourse(false, DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = ZerofiltreUtilsTest.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        lesson = lessonService.init(TITLE, chapter.getId(), author.getId());

        assertThat(lesson.getNumber()).isEqualTo(1);

        Lesson modifiedLesson = new Lesson();
        modifiedLesson.setId(lesson.getId());
        modifiedLesson.setTitle(UPDATED_TITLE);
        modifiedLesson.setContent(UPDATED_CONTENT);
        modifiedLesson.setSummary(UPDATED_SUMMARY);
        modifiedLesson.setThumbnail(THUMBNAIL);
        modifiedLesson.setVideo(VIDEO);
        modifiedLesson.setFree(true);
        modifiedLesson.setType("video");
        modifiedLesson.setChapterId(chapter.getId());

        //WHEN
        Lesson updatedLesson = lessonService.update(modifiedLesson, author.getId());

        //THEN
        assertThat(updatedLesson.getId()).isEqualTo(modifiedLesson.getId());
        assertThat(updatedLesson.getTitle()).isEqualTo(modifiedLesson.getTitle());
        assertThat(updatedLesson.getContent()).isEqualTo(modifiedLesson.getContent());
        assertThat(updatedLesson.getSummary()).isEqualTo(modifiedLesson.getSummary());
        assertThat(updatedLesson.getThumbnail()).isEqualTo(modifiedLesson.getThumbnail());
        assertThat(updatedLesson.getNumber()).isEqualTo(lesson.getNumber());
        assertThat(updatedLesson.getType()).isEqualTo("video");
        assertThat(updatedLesson.getVideo()).isEqualTo(modifiedLesson.getVideo());
        assertThat(updatedLesson.isFree()).isTrue();
        assertThat(updatedLesson.getType()).isEqualTo(modifiedLesson.getType());
        assertThat(updatedLesson.getChapterId()).isEqualTo(modifiedLesson.getChapterId());
        assertThat(updatedLesson.getResources()).isEmpty();
        assertThat(updatedLesson.isNotEnrolledAccess()).isFalse();

    }

    @Test
    void delete_lesson_isOk() throws ForbiddenActionException, ResourceNotFoundException {
        author = ZerofiltreUtilsTest.createMockUser(false);
        author = userProvider.save(author);

        course = ZerofiltreUtilsTest.createMockCourse(false, DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = ZerofiltreUtilsTest.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        lesson = lessonService.init(TITLE, chapter.getId(), author.getId());

        assertThat(lessonProvider.lessonOfId(lesson.getId())).isNotEmpty();


        lessonService.delete(lesson.getId(), author.getId());

        assertThat(lessonProvider.lessonOfId(lesson.getId())).isEmpty();

    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_Lesson_increases_course_lessonsCount() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        author = ZerofiltreUtilsTest.createMockUser(false);
        author.setEmail("test@mail.uk");
        author.setPseudoName("pseudo");
        author = userProvider.save(author);


        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);
        Course course = courseService.init("A course", author, 0);

        chapter = ZerofiltreUtilsTest.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        Chapter chapter2 = ZerofiltreUtilsTest.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);
        chapter2 = chapterProvider.save(chapter2);

        lessonService.init(TITLE, chapter.getId(), author.getId());
        lessonService.init(TITLE_2, chapter2.getId(), author.getId());

        //WHEN
        course = courseProvider.courseOfId(course.getId()).get();

        //THEN
        assertThat(course.getLessonsCount()).isEqualTo(2);
    }

}
