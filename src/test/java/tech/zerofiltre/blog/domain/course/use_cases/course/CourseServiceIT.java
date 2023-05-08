package tech.zerofiltre.blog.domain.course.use_cases.course;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.*;
import tech.zerofiltre.blog.infra.providers.database.course.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.infra.providers.logging.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
@Import({DBCourseProvider.class, DBUserProvider.class, DBSectionProvider.class, DBTagProvider.class, DBChapterProvider.class,
        Slf4jLoggerProvider.class, DBEnrollmentProvider.class})
class CourseServiceIT {

    public static final String UPDATED_TITLE = "This is the updated title";
    public static final String UPDATED_SUB_TITLE = "This is the updated sub title";
    public static final String UPDATED_SUMMARY = "This is the updated summary";
    public static final String UPDATED_VIDEO = "updated video";
    public static final String UPDATED_THUMBNAIL = "updated thumbnail";

    @Autowired
    private CourseProvider courseProvider;

    @Autowired
    private SectionProvider sectionProvider;

    @Autowired
    EnrollmentProvider enrollmentProvider;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private TagProvider tagProvider;

    @Autowired
    private LoggerProvider loggerProvider;

    @Autowired
    private ChapterProvider chapterProvider;

    private Course course;

    private User author;

    private List<Section> sections;
    private List<Tag> tags;

    @BeforeEach
    void init() {
        sections = new ArrayList<>();
        tags = new ArrayList<>();
    }


    @Test
    void save_and_init_are_OK() throws ForbiddenActionException, ResourceNotFoundException {
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        ZerofiltreUtils.createMockTags(false)
                .forEach(tag -> tags.add(tagProvider.save(tag)));

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);

        course = courseService.init("some title", author);

        Section section1 = ZerofiltreUtils.createMockSection(course.getId(), sectionProvider, courseProvider, false);
        section1.init(author);

        course.setTags(tags);
        course.setSections(List.of(section1));
        course.setTitle(UPDATED_TITLE);
        course.setStatus(Status.PUBLISHED);
        course.setThumbnail(UPDATED_THUMBNAIL);
        course.setVideo(UPDATED_VIDEO);
        course.setSummary(UPDATED_SUMMARY);
        course.setSubTitle(UPDATED_SUB_TITLE);

        course = courseService.save(course, author);

        assertThat(course.getId()).isNotZero();
        assertThat(course.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(course.getSubTitle()).isEqualTo(UPDATED_SUB_TITLE);
        assertThat(course.getSummary()).isEqualTo(UPDATED_SUMMARY);
        assertThat(course.getVideo()).isEqualTo(UPDATED_VIDEO);
        assertThat(course.getThumbnail()).isEqualTo(UPDATED_THUMBNAIL);
        assertThat(course.getStatus()).isEqualTo(Status.IN_REVIEW);

        assertThat(
                course.getSections().stream().allMatch(section -> sections.stream().anyMatch(s -> s.getId() == section.getId()))
        ).isTrue();

        assertThat(
                course.getTags().stream().allMatch(tag -> tags.stream().anyMatch(tag1 -> tag1.getId() == tag.getId()))
        ).isTrue();
    }

    @Test
    void getCourseById_isOK() throws ResourceNotFoundException, ForbiddenActionException {
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);


        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        Course course = courseService.init("some title", author);

        assertThat(courseService.findById(course.getId(), author)).isNotNull();
    }

    @Test
    @Disabled("to fix")
    void deleteCourse_isOK() throws ResourceNotFoundException, ForbiddenActionException {
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        ZerofiltreUtils.createMockSections(sectionProvider, courseProvider, false)
                .forEach(section -> sections.add(sectionProvider.save(section)));

        ZerofiltreUtils.createMockTags(false)
                .forEach(tag -> tags.add(tagProvider.save(tag)));

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        course = courseService.init("some title", author);

        courseService.delete(course.getId(), author);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.findById(course.getId(), author));
    }

    @Test
    void enrollOrSuspend_increasesOrDecreases_EnrolledCount() throws ZerofiltreException {
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        User student1 = ZerofiltreUtils.createMockUser(false);
        student1.setEmail("bable@gma.fr");
        student1.setPseudoName("bable");
        student1 = userProvider.save(student1);

        User student2 = ZerofiltreUtils.createMockUser(false);
        student2.setEmail("poseidon@gma.fr");
        student2.setPseudoName("poseidon");
        student2 = userProvider.save(student2);

        User student3 = ZerofiltreUtils.createMockUser(false);
        student3.setEmail("chaka@gma.fr");
        student3.setPseudoName("zulu");
        student3 = userProvider.save(student3);

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider, chapterProvider);
        course = courseService.init("some title", author);
        course.setStatus(Status.PUBLISHED);
        course = courseService.save(course, author);


        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCourse(course);
        enrollment1.setUser(student1);
        enrollmentProvider.save(enrollment1);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCourse(course);
        enrollment2.setUser(student2);
        enrollmentProvider.save(enrollment2);


        Enrollment suspendedEnrollment = new Enrollment();
        suspendedEnrollment.setCourse(course);
        suspendedEnrollment.setUser(student3);
        suspendedEnrollment = enrollmentProvider.save(suspendedEnrollment);

        int enrolledCount = courseService.getEnrolledCount(course.getId());

        assertThat(enrolledCount).isEqualTo(3);

        suspendedEnrollment.setActive(false);
        enrollmentProvider.save(suspendedEnrollment);

        enrolledCount = courseService.getEnrolledCount(course.getId());

        assertThat(enrolledCount).isEqualTo(2);
    }
}
