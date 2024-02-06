package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.EnrollmentJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.course.model.EnrollmentJPA;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import({DBUserProvider.class, DBCourseProvider.class, DBEnrollmentProvider.class, DBChapterProvider.class, DBLessonProvider.class})
class DBEnrollmentProviderIT {

    @Autowired
    private DBEnrollmentProvider dbEnrollmentProvider;
    @Autowired
    private DBUserProvider userProvider;
    @Autowired
    private DBCourseProvider courseProvider;
    @Autowired
    private DBChapterProvider chapterProvider;
    @Autowired
    private DBLessonProvider lessonProvider;

    EnrollmentJPAMapper enrollmentJPAMapper = Mappers.getMapper(EnrollmentJPAMapper.class);
    @Autowired
    private EnrollmentJPARepository enrollmentJPARepository;


    @Test
    void of_returns_ProperPageOfEnrollment() throws ZerofiltreException {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Course course1 = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course1 = courseProvider.save(course1);

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setUser(user);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCourse(course1);
        enrollment1.setUser(user);

        dbEnrollmentProvider.save(enrollment);
        dbEnrollmentProvider.save(enrollment1);

        //when
        Page<Enrollment> result = dbEnrollmentProvider.of(0, 2, user.getId(), null, null);


        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(2);
        assertThat(result.getTotalNumberOfElements()).isEqualTo(2);
        assertThat(result.getTotalNumberOfPages()).isEqualTo(1);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getHasNext()).isFalse();

    }

    @Test
    void saveEnrollment_savesCompletedLessons_Properly() throws ZerofiltreException {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setUser(user);

        enrollment = dbEnrollmentProvider.save(enrollment);

        Chapter chapter = Chapter.builder().build();
        chapter = chapterProvider.save(chapter);

        Lesson lesson = new Lesson.LessonBuilder()
                .title("lesson")
                .chapterId(chapter.getId())
                .build();

        lesson = lessonProvider.save(lesson);


        CompletedLesson completedLesson = new CompletedLesson();
        completedLesson.setCompletedAt(LocalDateTime.now());
        completedLesson.setEnrollmentId(enrollment.getId());
        completedLesson.setLessonId(lesson.getId());

        enrollment.setCompletedLessons(Collections.singletonList(completedLesson));

        EnrollmentJPA jpa = enrollmentJPAMapper.toJPA(enrollment);
        EnrollmentJPA enrollmentJPA = enrollmentJPARepository.save(jpa);

        Lesson finalLesson = lesson;
        Enrollment finalEnrollment = enrollment;
        enrollmentJPA.getCompletedLessons().forEach(completedLessonJPA -> {
            assertThat(finalLesson.getId()).isEqualTo(completedLessonJPA.getLesson().getId());
            assertThat(finalEnrollment.getId()).isEqualTo(completedLessonJPA.getEnrollment().getId());
        });

    }

    @Test
    void of_does_not_return_inactive_enrollments() throws ZerofiltreException {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Course course1 = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course1 = courseProvider.save(course1);

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setUser(user);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCourse(course1);
        enrollment1.setUser(user);

        dbEnrollmentProvider.save(enrollment);
        enrollment1 = dbEnrollmentProvider.save(enrollment1);
        enrollment1.setActive(false);
        dbEnrollmentProvider.save(enrollment1);

        //when
        Page<Enrollment> result = dbEnrollmentProvider.of(0, 2, user.getId(), null, null);


        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getTotalNumberOfElements()).isEqualTo(1);

    }
}
