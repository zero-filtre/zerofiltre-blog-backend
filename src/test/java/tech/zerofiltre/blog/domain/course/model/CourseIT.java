package tech.zerofiltre.blog.domain.course.model;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
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
@Import({DBCourseProvider.class, DBUserProvider.class, DBSectionProvider.class, DBTagProvider.class,
        Slf4jLoggerProvider.class})
class CourseIT {

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
    private UserProvider userProvider;

    @Autowired
    private TagProvider tagProvider;

    @Autowired
    private LoggerProvider loggerProvider;

    private Course course;

    private User author;

    private List<Section> sections = new ArrayList<>();
    private List<Tag> tags = new ArrayList<>();


    @Test
    void save_and_init_are_OK() throws ForbiddenActionException, ResourceNotFoundException {
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        ZerofiltreUtils.createMockSections(sectionProvider, false)
                .forEach(section -> sections.add(sectionProvider.save(section)));

        ZerofiltreUtils.createMockTags(false)
                .forEach(tag -> tags.add(tagProvider.save(tag)));


        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)
                .sectionProvider(sectionProvider)
                .build()
                .init("some title", author);
        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)
                .sectionProvider(sectionProvider)
                .tags(tags)
                .sections(sections)
                .status(Status.PUBLISHED)
                .thumbnail(UPDATED_THUMBNAIL)
                .video(UPDATED_VIDEO)
                .summary(UPDATED_SUMMARY)
                .subTitle(UPDATED_SUB_TITLE)
                .title(UPDATED_TITLE)
                .id(course.getId())
                .build()
                .save(author.getId());

        assertThat(course.getId()).isNotZero();
        assertThat(course.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(course.getSubTitle()).isEqualTo(UPDATED_SUB_TITLE);
        assertThat(course.getSummary()).isEqualTo(UPDATED_SUMMARY);
        assertThat(course.getVideo()).isEqualTo(UPDATED_VIDEO);
        assertThat(course.getThumbnail()).isEqualTo(UPDATED_THUMBNAIL);
        assertThat(course.getStatus()).isEqualTo(Status.IN_REVIEW);

        assertThat(
                course.getSections().stream().allMatch(section -> sections.stream().anyMatch(section1 -> section1.getId() == section.getId()))
        ).isTrue();

        assertThat(
                course.getTags().stream().allMatch(tag -> tags.stream().anyMatch(tag1 -> tag1.getId() == tag.getId()))
        ).isTrue();
    }

    @Test
    void getCourseById_isOK() throws ResourceNotFoundException, ForbiddenActionException {
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        ZerofiltreUtils.createMockSections(sectionProvider, false)
                .forEach(section -> sections.add(sectionProvider.save(section)));

        ZerofiltreUtils.createMockTags(false)
                .forEach(tag -> tags.add(tagProvider.save(tag)));

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)
                .sectionProvider(sectionProvider)
                .build()
                .init("some title", author);

        assertThat(course.findById(course.getId(), author)).isNotNull();
    }

    @Test
    void deleteCourse_isOK() throws ResourceNotFoundException, ForbiddenActionException {
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        ZerofiltreUtils.createMockSections(sectionProvider, false)
                .forEach(section -> sections.add(sectionProvider.save(section)));

        ZerofiltreUtils.createMockTags(false)
                .forEach(tag -> tags.add(tagProvider.save(tag)));

        course = new Course.CourseBuilder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .tagProvider(tagProvider)
                .sectionProvider(sectionProvider)
                .loggerProvider(loggerProvider)
                .build()
                .init("some title", author);

        course.delete(course.getId(), author);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> course.findById(course.getId(), author));
    }
}
