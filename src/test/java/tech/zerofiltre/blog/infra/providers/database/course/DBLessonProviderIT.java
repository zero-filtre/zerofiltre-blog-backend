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

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
@Import({DBChapterProvider.class, DBCourseProvider.class, DBUserProvider.class, DBEnrollmentProvider.class})
class DBLessonProviderIT {

    DBLessonProvider lessonProvider;

    @Autowired
    LessonJPARepository lessonJPARepository;

    @Autowired
    EnrollmentJPARepository enrollmentJPARepository;

    @Autowired
    DBUserProvider dbUserProvider;

    @Autowired
    DBCourseProvider dbCourseProvider;

    @Autowired
    DBChapterProvider dbChapterProvider;

    @Autowired
    DBEnrollmentProvider dbEnrollmentProvider;

    @BeforeEach
    void setUp() {
        lessonProvider = new DBLessonProvider(lessonJPARepository, enrollmentJPARepository);
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
    void delete_lesson_removes_it_from_enrollment_completedLessons() throws ZerofiltreException {
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

        Enrollment enrollment = ZerofiltreUtils.createMockEnrollment(false, author, course);
        dbEnrollmentProvider.save(enrollment);

        //when
        lessonProvider.delete(lesson);

        //then
        assertThat(lessonProvider.lessonOfId(lesson.getId())).isEmpty();
        Optional<Enrollment> updatedEnrollment = dbEnrollmentProvider.enrollmentOf(author.getId(), course.getId(), true);
        assertThat(updatedEnrollment).isPresent();
        AssertionsForClassTypes.assertThat(updatedEnrollment.get().getCompletedLessons().size()).isEqualTo(0);
    }

    @Test
    void list_all_lessonId_notCompleted_by_enrollmentId_then_return_one_lessonId() throws ZerofiltreException {
        //given
        User author = ZerofiltreUtils.createMockUser(false);
        author = dbUserProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course = dbCourseProvider.save(course);

        Enrollment enrollment = ZerofiltreUtils.createMockEnrollment(false, author, course);
        enrollment = dbEnrollmentProvider.save(enrollment);

        Chapter chapter = ZerofiltreUtils.createMockChapter(false, dbChapterProvider, Collections.emptyList(), course.getId());
        chapter = dbChapterProvider.save(chapter);

        Lesson lesson1 = Lesson.builder()
                .chapterId(chapter.getId())
                .build();

        Lesson lesson2 = Lesson.builder()
                .chapterId(chapter.getId())
                .build();

        lesson1 = lessonProvider.save(lesson1);
        lesson2 = lessonProvider.save(lesson2);

        assertThat(lessonProvider.lessonOfId(lesson1.getId())).isPresent();
        assertThat(lessonProvider.lessonOfId(lesson2.getId())).isPresent();

        CompletedLesson completedLesson1 = new CompletedLesson();
        completedLesson1.setLessonId(lesson1.getId());
        completedLesson1.setCompletedAt(LocalDateTime.now());
        completedLesson1.setEnrollmentId(enrollment.getId());

        enrollment.getCompletedLessons().add(completedLesson1);

        enrollment = dbEnrollmentProvider.save(enrollment);

        Course course2 = new Course();
        course2 = dbCourseProvider.save(course2);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCourse(course2);
        enrollment2 = dbEnrollmentProvider.save(enrollment2);


        Chapter chapter2 = Chapter.builder()
                .courseId(course2.getId())
                .build();
        chapter2 = dbChapterProvider.save(chapter2);

        Lesson lesson3 = Lesson.builder()
                .chapterId(chapter2.getId())
                .build();

        lesson3 = lessonProvider.save(lesson3);

        assertThat(lessonProvider.lessonOfId(lesson3.getId())).isPresent();

        //when
        List<Long> response = lessonProvider.listNotCompletedLessons(enrollment.getId());

        //then
        assertThat(response.size()).isOne();
        assertThat(response.get(0)).isEqualTo(lesson2.getId());
    }

    @Test
    void list_all_lessonId_notCompleted_by_enrollmentId_then_return_nothing() throws ZerofiltreException {
        //given
        User author = ZerofiltreUtils.createMockUser(false);
        author = dbUserProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course = dbCourseProvider.save(course);

        Chapter chapter = ZerofiltreUtils.createMockChapter(false, dbChapterProvider, Collections.emptyList(), course.getId());
        chapter = dbChapterProvider.save(chapter);

        Lesson lesson1 = Lesson.builder()
                .chapterId(chapter.getId())
                .build();

        Lesson lesson2 = Lesson.builder()
                .chapterId(chapter.getId())
                .build();

        lesson1 = lessonProvider.save(lesson1);
        lesson2 = lessonProvider.save(lesson2);

        assertThat(lessonProvider.lessonOfId(lesson1.getId())).isPresent();
        assertThat(lessonProvider.lessonOfId(lesson2.getId())).isPresent();

        Enrollment enrollment = ZerofiltreUtils.createMockEnrollment(false, author, course);
        enrollment = dbEnrollmentProvider.save(enrollment);

        CompletedLesson completedLesson1 = new CompletedLesson();
        completedLesson1.setLessonId(lesson1.getId());
        completedLesson1.setCompletedAt(LocalDateTime.now());
        completedLesson1.setEnrollmentId(enrollment.getId());

        enrollment.getCompletedLessons().add(completedLesson1);

        CompletedLesson completedLesson2 = new CompletedLesson();
        completedLesson2.setLessonId(lesson2.getId());
        completedLesson2.setCompletedAt(LocalDateTime.now());
        completedLesson2.setEnrollmentId(enrollment.getId());

        enrollment.getCompletedLessons().add(completedLesson2);

        enrollment = dbEnrollmentProvider.save(enrollment);

        Course course2 = new Course();
        course2 = dbCourseProvider.save(course2);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCourse(course2);
        enrollment2 = dbEnrollmentProvider.save(enrollment2);


        Chapter chapter2 = Chapter.builder()
                .courseId(course2.getId())
                .build();
        chapter2 = dbChapterProvider.save(chapter2);

        Lesson lesson3 = Lesson.builder()
                .chapterId(chapter2.getId())
                .build();

        lesson3 = lessonProvider.save(lesson3);

        assertThat(lessonProvider.lessonOfId(lesson3.getId())).isPresent();

        //when
        List<Long> response = lessonProvider.listNotCompletedLessons(enrollment.getId());

        //then
        assertThat(response.size()).isZero();
    }
}
