package tech.zerofiltre.blog.infra.providers.database.course;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Reaction;
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
import tech.zerofiltre.blog.infra.providers.database.article.DBReactionProvider;
import tech.zerofiltre.blog.infra.providers.database.article.ReactionCourseJPARepository;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.database.user.UserJPARepository;
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static tech.zerofiltre.blog.domain.article.model.Status.DRAFT;
import static tech.zerofiltre.blog.domain.article.model.Status.PUBLISHED;
import static tech.zerofiltre.blog.util.ZerofiltreUtilsTest.TEST_FULL_NAME;
import static tech.zerofiltre.blog.util.ZerofiltreUtilsTest.TEST_PROFILE_PICTURE;

@DataJpaTest
class DBCourseProviderIT {

    DBCourseProvider courseProvider;

    DBUserProvider userProvider;

    DBChapterProvider chapterProvider;

    DBLessonProvider lessonProvider;

    DBReactionProvider reactionProvider;

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

    @Autowired
    private ReactionCourseJPARepository reactionRepository;


    @BeforeEach
    void init() {
        courseProvider = new DBCourseProvider(courseJPARepository, userJPARepository);
        userProvider = new DBUserProvider(userJPARepository);
        lessonProvider = new DBLessonProvider(lessonJPARepository, enrollmentJPARepository);
        chapterProvider = new DBChapterProvider(chapterJPARepository);
    }

    @Test
    void savingACourse_isOK() {
        User author = ZerofiltreUtilsTest.createMockUser(false);
        author = userProvider.save(author);

        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course.setMentored(true);
        course = courseProvider.save(course);

        assertThat(course.getId()).isNotZero();
        assertThat(course.isMentored()).isTrue();
    }

    @Test
    void getACourseByItsId_isOk() {
        User author = ZerofiltreUtilsTest.createMockUser(false);
        author = userProvider.save(author);

        Course course = ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Optional<Course> courseOptional = courseProvider.courseOfId(course.getId());

        assertThat(courseOptional).isPresent();
        assertThat(courseOptional.get().getId()).isEqualTo(course.getId());
        assertThat(courseOptional.get().isMentored()).isFalse();
    }

    @Test
    void getEnrolledCount_GetsEnrollments_ActiveAndNotActive() throws ZerofiltreException {
        //given
        Course course = initCourse2Enrollments(true, false);

        //when
        long enrolledCount = courseProvider.getEnrolledCount(course.getId());

        //then
        assertThat(enrolledCount).isEqualTo(2);


    }

    @Test
    void getEnrolledCount_GetsEnrollments_CompletedAndNotCompleted() throws ZerofiltreException {
        //given
        Course course = initCourse2Enrollments(true, true);

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

    @Test
    void courseOf_works_properly() throws ZerofiltreException {

        //given
        initCourseWithReactions();

        initCourse2Enrollments(false, false);

        //when
        Page<Course> result = courseProvider.courseOf(0, 10, PUBLISHED, 0, null, null);

        //then
        assertThat(result).isNotNull();
        List<Course> content = result.getContent();
        Assertions.assertThat(content).hasSize(2);
        Course oldest = content.get(1);
        Course newest = content.get(0);

        assertThat(oldest.getAuthor().getId()).isNotZero();
        assertThat(oldest.getAuthor().getFullName()).isEqualTo("first");
        assertThat(oldest.getAuthor().getProfilePicture()).isEqualTo("picture");

        assertThat(newest.getAuthor().getId()).isNotZero();
        assertThat(newest.getAuthor().getFullName()).isEqualTo(TEST_FULL_NAME);
        assertThat(newest.getAuthor().getProfilePicture()).isEqualTo(TEST_PROFILE_PICTURE);

    }

    @Test
    @DisplayName("When I'm looking for new courses from last month, I return the list")
    void shouldReturnList_whenSearchingNewCoursesFromLastMonth() {
        //ARRANGE
        // -- dates
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        LocalDateTime threeMonthsBack = LocalDateTime.now().minusMonths(3);

        // -- Users
        User userA = new User();
        userA = userProvider.save(userA);

        User userB = new User();
        userB = userProvider.save(userB);

        // -- Courses
        Course courseA1 = new Course();
        courseA1.setAuthor(userA);
        courseA1.setLastPublishedAt(threeMonthsBack);
        courseProvider.save(courseA1);

        Course courseA2 = new Course();
        courseA2.setAuthor(userA);
        courseA2.setLastPublishedAt(lastMonth);
        courseA2 = courseProvider.save(courseA2);

        Course courseA3 = new Course();
        courseA3.setAuthor(userA);
        courseA3.setLastPublishedAt(lastMonth);
        courseA3 = courseProvider.save(courseA3);

        Course courseB1 = new Course();
        courseB1.setAuthor(userB);
        courseB1.setLastPublishedAt(threeMonthsBack);
        courseProvider.save(courseB1);

        Course courseB2 = new Course();
        courseB2.setAuthor(userB);
        courseB2.setLastPublishedAt(lastMonth);
        courseB2 = courseProvider.save(courseB2);

        //ACT
        List<Course> courseList = courseProvider.newCoursesFromLastMonth();

        //ASSERT
        assertThat(courseList.size()).isEqualTo(3);

        assertThat(courseList.get(0).getId()).isEqualTo(courseA2.getId());
        assertThat(courseList.get(0).getLastPublishedAt()).isEqualTo(courseA2.getLastPublishedAt());

        assertThat(courseList.get(1).getId()).isEqualTo(courseA3.getId());
        assertThat(courseList.get(1).getLastPublishedAt()).isEqualTo(courseA3.getLastPublishedAt());

        assertThat(courseList.get(2).getId()).isEqualTo(courseB2.getId());
        assertThat(courseList.get(2).getLastPublishedAt()).isEqualTo(courseB2.getLastPublishedAt());
    }

    private List<Reaction> initCourseWithReactions() {
        User author = ZerofiltreUtilsTest.createMockUser(false);
        author.setFullName("first");
        author.setProfilePicture("picture");
        author = userProvider.save(author);

        Course course = ZerofiltreUtilsTest.createMockCourse(false, PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course.setPublishedAt(LocalDateTime.now());
        course.setEnrolledCount(0);
        course = courseProvider.save(course);

        reactionProvider = new DBReactionProvider(null, reactionRepository);
        List<Reaction> reactions = ZerofiltreUtilsTest.createMockReactions(false, 0, course.getId(), author);
        for (Reaction reaction : reactions) {
            reactionProvider.save(reaction);
        }
        return reactions;
    }


    private Course initCourse2Enrollments(boolean withThe2ndOneInactive, boolean withThe2ndOneCompleted) throws ZerofiltreException {
        EnrollmentProvider enrollmentProvider = new DBEnrollmentProvider(enrollmentJPARepository);
        UserProvider userProvider = new DBUserProvider(userJPARepository);
        CourseProvider courseProvider = new DBCourseProvider(courseJPARepository, userJPARepository);


        User author = ZerofiltreUtilsTest.createMockUser(false);
        author.setPseudoName("author");
        author.setEmail("author@gmail.fr");
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user.setPseudoName("enrolled");
        user.setEmail("susbscriber@gamil.fr");

        User secondUser = ZerofiltreUtilsTest.createMockUser(false);
        secondUser.setPseudoName("enrolledSecond");
        secondUser.setEmail("susbscriberSecond@gamil.fr");


        author = userProvider.save(author);
        user = userProvider.save(user);
        secondUser = userProvider.save(secondUser);

        Course course = ZerofiltreUtilsTest.createMockCourse(false, PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course.setPublishedAt(LocalDateTime.now());
        course = courseProvider.save(course);

        Enrollment enrollment = ZerofiltreUtilsTest.createMockEnrollment(false, user, course);
        enrollmentProvider.save(enrollment);

        Enrollment sencondEnrollment = ZerofiltreUtilsTest.createMockEnrollment(false, secondUser, course);
        sencondEnrollment.setActive(!withThe2ndOneInactive);
        sencondEnrollment.setCompleted(withThe2ndOneCompleted);
        enrollmentProvider.save(sencondEnrollment);
        return course;
    }

    private Course initCourseWith2Lessons() throws ForbiddenActionException, ResourceNotFoundException {
        User author = ZerofiltreUtilsTest.createMockUser(false);
        author.setPseudoName("author");
        author.setEmail("author@gmail.fr");
        author = userProvider.save(author);

        Course course = ZerofiltreUtilsTest.createMockCourse(false, DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);


        Chapter chapter = ZerofiltreUtilsTest.createMockChapter(false, chapterProvider, userProvider, lessonProvider, courseProvider, Collections.emptyList(), course.getId());
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
