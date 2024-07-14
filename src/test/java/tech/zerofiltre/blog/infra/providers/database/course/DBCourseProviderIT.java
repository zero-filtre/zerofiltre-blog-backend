package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Chapter;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.course.model.Lesson;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.database.user.UserJPARepository;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static tech.zerofiltre.blog.domain.article.model.Status.DRAFT;
import static tech.zerofiltre.blog.domain.article.model.Status.PUBLISHED;

@DataJpaTest
class DBCourseProviderIT {

    DBCourseProvider courseProvider;

    DBUserProvider userProvider;

    DBChapterProvider chapterProvider;

    DBLessonProvider lessonProvider;

    @Autowired
    CourseJPARepository courseJPARepository;

    @Autowired
    UserJPARepository userJPARepository;

    @Autowired
    private EnrollmentJPARepository enrollmentJPARepository;

    @Autowired
    private LessonJPARepository lessonJPARepository;

    @Autowired
    private ChapterJPARepository chapterJPARepository;


    @BeforeEach
    void init() {
        courseProvider = new DBCourseProvider(courseJPARepository);
        userProvider = new DBUserProvider(userJPARepository);
        lessonProvider = new DBLessonProvider(lessonJPARepository, enrollmentJPARepository);
        chapterProvider = new DBChapterProvider(chapterJPARepository);
    }

    @Test
    void savingACourse_isOK() {
        User author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course.setMentored(true);
        course = courseProvider.save(course);

        assertThat(course.getId()).isNotZero();
        assertThat(course.isMentored()).isTrue();
    }

    @Test
    void getACourseByItsId_isOk() {
        User author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Optional<Course> courseOptional = courseProvider.courseOfId(course.getId());

        assertThat(courseOptional).isPresent();
        assertThat(courseOptional.get().getId()).isEqualTo(course.getId());
        assertThat(courseOptional.get().isMentored()).isFalse();
    }

    @Test
    void getEnrolledCount_GetsEnrollments_ActiveAndNotActive() throws ZerofiltreException {
        //given
        Course course = init2Enrollments(true, false);

        //when
        long enrolledCount = courseProvider.getEnrolledCount(course.getId());

        //then
        assertThat(enrolledCount).isEqualTo(2);


    }

    @Test
    void getEnrolledCount_GetsEnrollments_CompletedAndNotCompleted() throws ZerofiltreException {
        //given
        Course course = init2Enrollments(true, true);

        //when
        long enrolledCount = courseProvider.getEnrolledCount(course.getId());

        //then
        assertThat(enrolledCount).isEqualTo(2);


    }

    @Test
    void getLessonsCount_works_properly() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        Course course = initCourseWith2Lessons();

        //when
        long lessonsCount = courseProvider.getLessonsCount(course.getId());

        //then
        assertThat(lessonsCount).isEqualTo(2);
    }


    private Course init2Enrollments(boolean withThe2ndOneInactive, boolean withThe2ndOneCompleted) throws ZerofiltreException {
        EnrollmentProvider enrollmentProvider = new DBEnrollmentProvider(enrollmentJPARepository);
        UserProvider userProvider = new DBUserProvider(userJPARepository);
        CourseProvider courseProvider = new DBCourseProvider(courseJPARepository);


        User author = ZerofiltreUtils.createMockUser(false);
        author.setPseudoName("author");
        author.setEmail("author@gmail.fr");
        User user = ZerofiltreUtils.createMockUser(false);
        user.setPseudoName("enrolled");
        user.setEmail("susbscriber@gamil.fr");

        User secondUser = ZerofiltreUtils.createMockUser(false);
        secondUser.setPseudoName("enrolledSecond");
        secondUser.setEmail("susbscriberSecond@gamil.fr");


        author = userProvider.save(author);
        user = userProvider.save(user);
        secondUser = userProvider.save(secondUser);

        Course course = ZerofiltreUtils.createMockCourse(false, PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Enrollment enrollment = ZerofiltreUtils.createMockEnrollment(false, user, course);
        enrollmentProvider.save(enrollment);

        Enrollment sencondEnrollment = ZerofiltreUtils.createMockEnrollment(false, secondUser, course);
        sencondEnrollment.setActive(!withThe2ndOneInactive);
        sencondEnrollment.setCompleted(withThe2ndOneCompleted);
        enrollmentProvider.save(sencondEnrollment);
        return course;
    }

    private Course initCourseWith2Lessons() throws ForbiddenActionException, ResourceNotFoundException {
        User author = ZerofiltreUtils.createMockUser(false);
        author.setPseudoName("author");
        author.setEmail("author@gmail.fr");
        author = userProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);


        Chapter chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, userProvider, lessonProvider, courseProvider, Collections.emptyList(), course.getId());
        chapter = chapter.init("new chapter", course.getId(), author.getId());

        Lesson lesson1 = Lesson.builder()
                .title("new lesson")
                .chapterId(chapter.getId())
                .build();
        lessonProvider.save(lesson1);

        Lesson lesson2 = Lesson.builder()
                .title("new lesson2")
                .chapterId(chapter.getId())
                .build();
        lessonProvider.save(lesson2);

        return course;

    }


}
