package tech.zerofiltre.blog.domain.course.model;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static tech.zerofiltre.blog.util.ZerofiltreUtils.*;

@DataJpaTest
@Import({DBSectionProvider.class, DBCourseProvider.class, DBUserProvider.class})
class SectionIT {

    private Section section;
    @Autowired
    private SectionProvider sectionProvider;
    @Autowired
    private CourseProvider courseProvider;
    @Autowired
    UserProvider userProvider;

    @Test
    void save_Section_IsOK() {

        //GIVEN
        section = ZerofiltreUtils.createMockSections(sectionProvider, false).get(0);

        //WHEN
        section = section.save();

        //THEN
        assertThat(section.getId()).isNotZero();

    }

    @Test
    void update_Section_IsOK() {

        //GIVEN
        section = new Section.SectionBuilder()
                .position(0)
                .title("title")
                .content("content")
                .image("image")
                .sectionProvider(sectionProvider)
                .build().save();


        //WHEN
        section = ZerofiltreUtils.createMockSections(sectionProvider, true)
                .get(0)
                .save();

        //THEN
        assertThat(section.getContent()).isEqualTo(TEST_SECTION_CONTENT_1);
        assertThat(section.getTitle()).isEqualTo(TEST_SECTION_TITLE_1);
        assertThat(section.getImage()).isEqualTo(TEST_THUMBNAIL);
        assertThat(section.getPosition()).isEqualTo(1);

    }

    @Test
    void deleteSection_isOk() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        User user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);
        Course course = new Course.CourseBuilder()
                .title("title")
                .status(Status.DRAFT)
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .sections(Collections.emptyList())
                .build()
                .init("", user);
        section = new Section.SectionBuilder()
                .position(0)
                .title("title")
                .content("content")
                .image("image")
                .courseId(course.getId())
                .courseProvider(courseProvider)
                .sectionProvider(sectionProvider)
                .userProvider(userProvider)
                .build().save();

        //WHEN
        section.delete(user);

        //THEN
        assertThat(sectionProvider.findById(section.getId())).isEmpty();
    }

    @Test
    void findSection_isOK() {
        //GIVEN
        section = new Section.SectionBuilder()
                .position(0)
                .title("title")
                .content("content")
                .image("image")
                .sectionProvider(sectionProvider)
                .build().save();

        //WHEN
        Optional<Section> sectionOptional = sectionProvider.findById(section.getId());

        //THEN
        assertThat(sectionOptional).isNotEmpty();
        assertThat(sectionOptional.get().getId()).isEqualTo(section.getId());
    }
}
