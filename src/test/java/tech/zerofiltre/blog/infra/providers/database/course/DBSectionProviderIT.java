package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
class DBSectionProviderIT {

    private DBSectionProvider sectionProvider;
    private DBCourseProvider courseProvider;
    @Autowired
    private SectionJPARepository sectionJPARepository;
    @Autowired
    private CourseJPARepository courseJPARepository;

    @BeforeEach
    void init() {
        sectionProvider = new DBSectionProvider(sectionJPARepository);
        courseProvider = new DBCourseProvider(courseJPARepository);
    }

    @Test
    void savingASection_isOK() {
        Section section = ZerofiltreUtils.createMockSections(sectionProvider, courseProvider, false).get(0);
        section = sectionProvider.save(section);

        assertThat(section.getId()).isNotZero();
    }

    @Test
    void getASectionByItsId_isOk() {
        Section section = ZerofiltreUtils.createMockSections(sectionProvider, courseProvider, false).get(0);
        section = sectionProvider.save(section);

        Optional<Section> sectionOptional = sectionProvider.findById(section.getId());

        assertThat(sectionOptional).isNotEmpty();
        assertThat(sectionOptional.get().getId()).isEqualTo(section.getId());
    }
}
