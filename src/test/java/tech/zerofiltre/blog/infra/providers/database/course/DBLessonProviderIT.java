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
    void delete_lesson_removes_it_from_enrollment_completedLessons() throws BlogException {
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
}
