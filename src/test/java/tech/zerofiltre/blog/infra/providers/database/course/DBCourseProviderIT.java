package tech.zerofiltre.blog.infra.providers.database.course;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Reaction;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
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
import tech.zerofiltre.blog.infra.providers.database.article.DBTagProvider;
import tech.zerofiltre.blog.infra.providers.database.article.ReactionCourseJPARepository;
import tech.zerofiltre.blog.infra.providers.database.article.TagJPARepository;
import tech.zerofiltre.blog.infra.providers.database.company.CompanyCourseJPARepository;
import tech.zerofiltre.blog.infra.providers.database.company.CompanyJPARepository;
import tech.zerofiltre.blog.infra.providers.database.company.DBCompanyCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.company.DBCompanyProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.database.user.UserJPARepository;
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
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

    DBCompanyProvider dbCompanyProvider;

    DBCompanyCourseProvider companyCourseProvider;

    DBTagProvider tagProvider;

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

    @Autowired
    private CompanyCourseJPARepository companyCourseJPARepository;

    @Autowired
    private CompanyJPARepository companyJPARepository;

    @Autowired
    private TagJPARepository tagJPARepository;


    @BeforeEach
    void init() {
        courseProvider = new DBCourseProvider(courseJPARepository, userJPARepository);
        userProvider = new DBUserProvider(userJPARepository);
        lessonProvider = new DBLessonProvider(lessonJPARepository, enrollmentJPARepository);
        chapterProvider = new DBChapterProvider(chapterJPARepository);
        companyCourseProvider = new DBCompanyCourseProvider(companyCourseJPARepository);
        companyCourseProvider = new DBCompanyCourseProvider(companyCourseJPARepository);
        dbCompanyProvider = new DBCompanyProvider(companyJPARepository);
        tagProvider = new DBTagProvider(tagJPARepository);
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

    public static Stream<Arguments> testDataProvider() {
        return Stream.of(
                // -- tag is null and author is 0 --
                arguments(FinderRequest.Filter.MOST_VIEWED, null, 0),
                arguments(FinderRequest.Filter.POPULAR, null, 0),
                //any other status
                arguments(FinderRequest.Filter.COMPLETED, null, 0),

                // -- tag is null and author is not 0 --
                arguments(FinderRequest.Filter.MOST_VIEWED, null, 1),
                arguments(FinderRequest.Filter.POPULAR, null, 1),
                //any other status
                arguments(FinderRequest.Filter.COMPLETED, null, 1),

                // -- tag is not null and author is 0
                arguments(null, "java", 0),

                // -- tag is not null and author is not 0
                arguments(null, "java", 1)


        );
    }


    @MethodSource("testDataProvider")
    @ParameterizedTest(name = "[{index}] Getting courses with filter: {0} with tag {1} for authorId {2}")
    void courseOf_returns_only_nonExclusiveCompanyCourses(FinderRequest.Filter filter, String tag, long authorId) {

        //given
        User author = ZerofiltreUtilsTest.createMockUser(false);
        author.setPseudoName("author");
        author.setEmail("author@gmail.fr");
        author = userProvider.save(author);

        if (authorId != 0) authorId = author.getId();

        Tag java = tagProvider.save(new Tag(0, "java"));

        Course linked = ZerofiltreUtilsTest.createMockCourse(false, PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        linked.setTags(List.of(java));
        linked = courseProvider.save(linked);

        Course linkedExclusive = ZerofiltreUtilsTest.createMockCourse(false, PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        linkedExclusive = courseProvider.save(linkedExclusive);

        Course shared = ZerofiltreUtilsTest.createMockCourse(false, PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        shared.setTags(List.of(java));
        shared = courseProvider.save(shared);


        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));

        companyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), linked.getId(), false, true, LocalDateTime.now(), null));
        companyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), linkedExclusive.getId(), true, true, LocalDateTime.now(), null));

        //when
        Page<Course> result = courseProvider.courseOf(0, 12, PUBLISHED, authorId, filter, tag);

        //assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalNumberOfElements()).isEqualTo(2);
        long linkedId = linked.getId();
        long sharedId = shared.getId();
        long linkedExclusiveId = linkedExclusive.getId();
        result.getContent().forEach(course -> {
            boolean sharedOrLinked = (course.getId() == linkedId || course.getId() == sharedId) && course.getId() != linkedExclusiveId;
            assertThat(sharedOrLinked).isTrue();
        });
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

    @Test
    @DisplayName("When I search for the id of the company that owns a course, this id returned")
    void shouldReturnIdCompany_whenCompanyOwnerCourse() {
        //GIVEN
        Course course = courseProvider.save(new Course());
        long companyId = 6;
        companyCourseProvider.save(new LinkCompanyCourse(0, companyId, course.getId(), true, true, LocalDateTime.now(), null));

        //WHEN
        Optional<Long> response = courseProvider.idOfCompanyOwningCourse(course.getId());

        //THEN
        assertThat(response).isPresent();
        assertThat(response.get()).isEqualTo(companyId);
    }

    @Test
    @DisplayName("when I search for the id of the company that doesn't own a course, an empty object is returned")
    void shouldReturnEmpty_whenCompanyNotOwnerCourse() {
        //GIVEN
        Course course = courseProvider.save(new Course());
        companyCourseProvider.save(new LinkCompanyCourse(0, 6, course.getId(), false, true, LocalDateTime.now(), null));

        //WHEN
        Optional<Long> response = courseProvider.idOfCompanyOwningCourse(course.getId());

        //THEN
        assertThat(response).isEmpty();
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

        Lesson lesson1 = Lesson.builder().title("new lesson").chapterId(chapter.getId()).build();
        lessonProvider.save(lesson1);

        Lesson lesson2 = Lesson.builder().title("new lesson2").chapterId(chapter.getId()).build();
        lessonProvider.save(lesson2);

        return course;

    }
}
