package tech.zerofiltre.blog.domain.course.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.SectionProvider;
import tech.zerofiltre.blog.domain.course.features.course.CourseService;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.article.DBTagProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBChapterProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBEnrollmentProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBSectionProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.logging.Slf4jLoggerProvider;
import tech.zerofiltre.blog.util.DataChecker;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static tech.zerofiltre.blog.util.ZerofiltreUtils.*;

@DataJpaTest
@Import({DBSectionProvider.class, Slf4jLoggerProvider.class, DBCourseProvider.class, DBUserProvider.class, DBTagProvider.class, DBChapterProvider.class, DBEnrollmentProvider.class})
class SectionIT {

    private Section section;
    @Autowired
    private SectionProvider sectionProvider;
    @Autowired
    private CourseProvider courseProvider;
    @Autowired
    private EnrollmentProvider enrollmentProvider;
    @Autowired
    TagProvider tagProvider;

    @Autowired
    ChapterProvider chapterProvider;

    @Autowired
    UserProvider userProvider;

    @Autowired
    LoggerProvider loggerProvider;

    @MockBean
    private DataChecker checker;

    @MockBean
    private CompanyCourseProvider companyCourseProvider;

    public static final String UPDATED_TITLE = "updated title";
    public static final String UPDATED_CONTENT = "updated content";
    public static final String UPDATED_IMAGE = "updated image";

    @Test
    void init_Section_IsOK() throws ForbiddenActionException, ResourceNotFoundException {

        //GIVEN
        User author = new User();
        author = userProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);
        User user = ZerofiltreUtils.createMockUser(true);
        user = userProvider.save(user);

        //WHEN
        section = Section.builder()
                .courseId(course.getId())
                .courseProvider(courseProvider)
                .sectionProvider(sectionProvider)
                .build()
                .init(user);

        //THEN
        assertThat(section.getId()).isNotZero();

    }

    @Test
    void update_Section_IsOK() throws ZerofiltreException {

        //GIVEN
        User author = new User();
        author = userProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);
        User user = ZerofiltreUtils.createMockUser(true);
        user = userProvider.save(user);

        section = new Section.SectionBuilder()
                .position(0)
                .title("title")
                .content("content")
                .image("image")
                .courseId(course.getId())
                .courseProvider(courseProvider)
                .sectionProvider(sectionProvider)
                .build().init(user);


        //WHEN
        section = ZerofiltreUtils.createMockSections(sectionProvider, courseProvider, true)
                .get(0)
                .update(section.getId(), TEST_SECTION_TITLE_1, TEST_SECTION_CONTENT_1, TEST_THUMBNAIL, 1, user);

        //THEN
        assertThat(section.getContent()).isEqualTo(TEST_SECTION_CONTENT_1);
        assertThat(section.getTitle()).isEqualTo(TEST_SECTION_TITLE_1);
        assertThat(section.getImage()).isEqualTo(TEST_THUMBNAIL);
        assertThat(section.getPosition()).isEqualTo(1);

    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void update_UpdatesFieldsProperly() throws ZerofiltreException {
        User author = ZerofiltreUtils.createMockUser(false);
        author.setEmail("test@mail.uk");
        author.setPseudoName("pseudo");
        author = userProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        section = new Section.SectionBuilder()
                .position(1)
                .title("title")
                .content("content")
                .image("image")
                .courseId(course.getId())
                .courseProvider(courseProvider)
                .sectionProvider(sectionProvider)
                .build().init(author);

        section = new Section.SectionBuilder()
                .sectionProvider(sectionProvider)
                .courseProvider(courseProvider)
                .id(section.getId())
                .build();


        section = section.update(section.getId(), UPDATED_TITLE, UPDATED_CONTENT, UPDATED_IMAGE, 999, author);

        assertThat(section.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(section.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(section.getImage()).isEqualTo(UPDATED_IMAGE);
        assertThat(section.getPosition()).isEqualTo(999);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void update_reordersSections_IfPositionIsChanged() throws ZerofiltreException {

        User author = ZerofiltreUtils.createMockUser(false);
        author.setEmail("update_reordersSections_IfPositionIsChanged@mail.com");
        author.setPseudoName("update_reordersSections_IfPositionIsChanged");
        author = userProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Section section = new Section.SectionBuilder()
                .position(1)
                .title("title")
                .content("content")
                .image("image")
                .courseId(course.getId())
                .sectionProvider(sectionProvider)
                .courseProvider(courseProvider)
                .build().init(author);

        Section section2 = new Section.SectionBuilder()
                .position(2)
                .title("title")
                .content("content")
                .image("image")
                .courseId(course.getId())
                .sectionProvider(sectionProvider)
                .courseProvider(courseProvider)
                .build().init(author);

        Section section3 = new Section.SectionBuilder()
                .position(3)
                .title("title")
                .content("content")
                .image("image")
                .courseId(course.getId())
                .sectionProvider(sectionProvider)
                .courseProvider(courseProvider)
                .build().init(author);

        section = section.update(section.getId(), UPDATED_TITLE, UPDATED_CONTENT, UPDATED_IMAGE, 2, author);

        assertThat(section.getPosition()).isEqualTo(2);

        section2 = sectionProvider.findById(section2.getId()).get();
        assertThat(section2.getPosition()).isEqualTo(1);

        section3 = sectionProvider.findById(section3.getId()).get();
        assertThat(section3.getPosition()).isEqualTo(3);

    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void update_DoesNotReorderSections_IfPositionIsSame() throws ZerofiltreException {

        User author = ZerofiltreUtils.createMockUser(false);
        author.setEmail("update_DoesNotReorderSections_IfPositionIsSame@mail.com");
        author.setPseudoName("update_DoesNotReorderSections_IfPositionIsSame@mail.com");
        author = userProvider.save(author);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Section section = new Section.SectionBuilder()
                .position(1)
                .title("title")
                .content("content")
                .image("image")
                .courseId(course.getId())
                .sectionProvider(sectionProvider)
                .courseProvider(courseProvider)
                .build().init(author);
        int position1 = section.getPosition();

        Section section2 = new Section.SectionBuilder()
                .position(2)
                .title("title")
                .content("content")
                .image("image")
                .courseId(course.getId())
                .sectionProvider(sectionProvider)
                .courseProvider(courseProvider)
                .build().init(author);
        int position2 = section2.getPosition();


        Section section3 = new Section.SectionBuilder()
                .position(3)
                .title("title")
                .content("content")
                .image("image")
                .courseId(course.getId())
                .sectionProvider(sectionProvider)
                .courseProvider(courseProvider)
                .build().init(author);
        int position3 = section3.getPosition();


        section = section.update(section.getId(), UPDATED_TITLE, UPDATED_CONTENT, UPDATED_IMAGE, 1, author);


        assertThat(section.getPosition()).isEqualTo(position1);
        assertThat(section.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(section.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(section.getImage()).isEqualTo(UPDATED_IMAGE);

        section2 = sectionProvider.findById(section2.getId()).get();
        assertThat(section2.getPosition()).isEqualTo(position2);

        section3 = sectionProvider.findById(section3.getId()).get();
        assertThat(section3.getPosition()).isEqualTo(position3);

    }

    @Test
    void deleteSection_isOk() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        User user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);

        Course course = courseService.init("", user, 0);

        section = new Section.SectionBuilder()
                .position(0)
                .title("title")
                .content("content")
                .image("image")
                .courseId(course.getId())
                .courseProvider(courseProvider)
                .sectionProvider(sectionProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .build().init(user);
        assertThat(sectionProvider.findById(section.getId())).isNotEmpty();

        //WHEN
        section.delete(user);

        //THEN
        assertThat(sectionProvider.findById(section.getId())).isEmpty();
    }

    @Test
    void findSection_isOK() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN

        User user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, checker, companyCourseProvider, enrollmentProvider);

        Course course = courseService.init("", user, 0);

        section = new Section.SectionBuilder()
                .position(0)
                .title("title")
                .content("content")
                .image("image")
                .courseId(course.getId())
                .courseProvider(courseProvider)
                .sectionProvider(sectionProvider)
                .build().init(user);

        //WHEN
        Optional<Section> sectionOptional = sectionProvider.findById(section.getId());

        //THEN
        assertThat(sectionOptional).isNotEmpty();
        assertThat(sectionOptional.get().getId()).isEqualTo(section.getId());
    }
}
