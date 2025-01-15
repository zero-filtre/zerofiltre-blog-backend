package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.features.enrollment.CompleteLesson;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.EnrollmentJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.course.model.EnrollmentJPA;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    private CompleteLesson completeLesson;

    @BeforeEach
    void init() {
        completeLesson = new CompleteLesson(dbEnrollmentProvider, lessonProvider, chapterProvider, courseProvider);
    }


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
        assertThat(result.getContent().size()).isOne();
        assertThat(result.getTotalNumberOfElements()).isEqualTo(1);

    }

    @Test
    void completes_Enrollment_Properly() throws ZerofiltreException {
        //given
        User user = new User();
        user.setFullName("Testeur Humain");

        user = userProvider.save(user);

        Course course = new Course();
        course.setTitle("Cours sur les tests");
        course.setStatus(Status.PUBLISHED);
        course.setAuthor(user);

        course = courseProvider.save(course);

        Chapter chapter = new Chapter.ChapterBuilder()
                .courseId(course.getId())
                .build();

        chapter = chapterProvider.save(chapter);

        Lesson lesson1 = new Lesson.LessonBuilder()
                .chapterId(chapter.getId())
                .build();

        Lesson lesson2 = new Lesson.LessonBuilder()
                .chapterId(chapter.getId())
                .build();

        lesson1 = lessonProvider.save(lesson1);
        lesson2 = lessonProvider.save(lesson2);

        chapter.getLessons().add(lesson1);
        chapter.getLessons().add(lesson2);

        chapterProvider.save(chapter);

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);

        dbEnrollmentProvider.save(enrollment);
        completeLesson.execute(course.getId(), lesson1.getId(), user.getId(), true);
        enrollment = completeLesson.execute(course.getId(), lesson2.getId(), user.getId(), true);

        assertThat(enrollment.isCompleted()).isTrue();

    }

    @Test
    void UpdatesCertificatePath_Properly() throws ZerofiltreException {
        //given
        User user = new User();
        user.setFullName("Testeur Humain");

        user = userProvider.save(user);

        Course course = new Course();
        course.setTitle("Cours sur les tests");
        course.setStatus(Status.PUBLISHED);
        course.setAuthor(user);

        course = courseProvider.save(course);

        Chapter chapter = new Chapter.ChapterBuilder()
                .courseId(course.getId())
                .build();

        chapter = chapterProvider.save(chapter);

        Lesson lesson1 = new Lesson.LessonBuilder()
                .chapterId(chapter.getId())
                .build();

        Lesson lesson2 = new Lesson.LessonBuilder()
                .chapterId(chapter.getId())
                .build();

        lesson1 = lessonProvider.save(lesson1);
        lesson2 = lessonProvider.save(lesson2);

        chapter.getLessons().add(lesson1);
        chapter.getLessons().add(lesson2);

        chapterProvider.save(chapter);

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setActive(true);

        dbEnrollmentProvider.save(enrollment);
        dbEnrollmentProvider.setCertificatePath("certificates/a_b_c.pdf", user.getId(), course.getId());

        Optional<Enrollment> updatedEnrollment = dbEnrollmentProvider.enrollmentOf(user.getId(), course.getId(), true);
        assertThat(updatedEnrollment).isPresent();
        assertThat(updatedEnrollment.get().getCertificatePath()).isEqualTo("certificates/a_b_c.pdf");

    }

    @Test
    void find() throws ZerofiltreException {
        //GIVEN
        User user = new User();
        user.setFullName("Testeur Humain");

        user = userProvider.save(user);

        Course course = new Course();
        course.setTitle("Cours sur les tests");
        course.setStatus(Status.PUBLISHED);
        course.setAuthor(user);

        course = courseProvider.save(course);

        Chapter chapter = new Chapter.ChapterBuilder()
                .courseId(course.getId())
                .build();

        chapter = chapterProvider.save(chapter);

        Lesson lesson1 = new Lesson.LessonBuilder()
                .chapterId(chapter.getId())
                .build();

        Lesson lesson2 = new Lesson.LessonBuilder()
                .chapterId(chapter.getId())
                .build();

        lesson1 = lessonProvider.save(lesson1);
        lesson2 = lessonProvider.save(lesson2);

        chapter.getLessons().add(lesson1);
        chapter.getLessons().add(lesson2);

        chapterProvider.save(chapter);

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setActive(true);
        enrollment.setCompanyCourseId(1);

        dbEnrollmentProvider.save(enrollment);

        //WHEN
        Optional<Enrollment> response = dbEnrollmentProvider.find(enrollment.getCompanyCourseId(), true);

        //THEN
        assertThat(response).isPresent();
        assertThat(response.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(response.get().getCourse().getId()).isEqualTo(course.getId());
    }

    @Test
    void findAllByCompanyCourseId_AndActive_Properly() throws ZerofiltreException {
        //GIVEN
        User user1 = new User();
        user1.setFullName("Testeur Humain");

        user1 = userProvider.save(user1);

        Course course1 = new Course();
        course1.setTitle("Cours sur les tests");
        course1.setStatus(Status.PUBLISHED);
        course1.setAuthor(user1);

        course1 = courseProvider.save(course1);

        User user2 = new User();
        user2.setFullName("Testeur Humain");

        user2 = userProvider.save(user2);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setUser(user1);
        enrollment1.setCourse(course1);
        enrollment1.setActive(true);
        enrollment1.setCompanyCourseId(1);

        dbEnrollmentProvider.save(enrollment1);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setUser(user2);
        enrollment2.setCourse(course1);
        enrollment2.setActive(true);
        enrollment2.setCompanyCourseId(1);

        dbEnrollmentProvider.save(enrollment2);

        //WHEN
        List<Enrollment> response = dbEnrollmentProvider.findAll(enrollment1.getCompanyCourseId(), true);

        //THEN
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getCompanyCourseId()).isEqualTo(enrollment1.getCompanyCourseId());
        assertThat(response.get(1).getCompanyCourseId()).isEqualTo(enrollment2.getCompanyCourseId());
    }

    @Test
    void findAllByCompanyUserId_AndActive_Properly() throws ZerofiltreException {
        //GIVEN
        User user1 = new User();
        user1.setFullName("Testeur Humain");

        user1 = userProvider.save(user1);

        Course course1 = new Course();
        course1.setTitle("Cours sur les tests");
        course1.setStatus(Status.PUBLISHED);
        course1.setAuthor(user1);

        course1 = courseProvider.save(course1);

        Course course2 = new Course();
        course2.setTitle("Cours sur les tests");
        course2.setStatus(Status.PUBLISHED);
        course2.setAuthor(user1);

        course2 = courseProvider.save(course2);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setUser(user1);
        enrollment1.setCourse(course1);
        enrollment1.setActive(true);
        enrollment1.setCompanyUserId(1);

        dbEnrollmentProvider.save(enrollment1);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setUser(user1);
        enrollment2.setCourse(course2);
        enrollment2.setActive(true);
        enrollment2.setCompanyUserId(1);

        dbEnrollmentProvider.save(enrollment2);

        //WHEN
        List<Enrollment> response = dbEnrollmentProvider.findAllByCompanyUserId(enrollment1.getCompanyUserId(), true);

        //THEN
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getCompanyUserId()).isEqualTo(enrollment1.getCompanyUserId());
        assertThat(response.get(1).getCompanyUserId()).isEqualTo(enrollment2.getCompanyUserId());
    }

    @Test
    void isCompleted() throws ZerofiltreException {
        //GIVEN
        User user = new User();
        user.setFullName("Testeur Humain");

        user = userProvider.save(user);

        Course course = new Course();
        course.setTitle("Cours sur les tests");
        course.setStatus(Status.PUBLISHED);
        course.setAuthor(user);

        course = courseProvider.save(course);

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setCompleted(true);

        dbEnrollmentProvider.save(enrollment);

        //WHEN
        boolean response = dbEnrollmentProvider.isCompleted(user.getId(), course.getId());

        //THEN
        assertThat(response).isTrue();
    }

}
