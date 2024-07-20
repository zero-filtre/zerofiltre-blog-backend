package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tech.zerofiltre.blog.domain.course.model.Section;
import tech.zerofiltre.blog.infra.providers.database.user.UserJPARepository;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class DBSectionProviderIT {

    private DBSectionProvider sectionProvider;
    private DBCourseProvider courseProvider;
    @Autowired
    private SectionJPARepository sectionJPARepository;
    @Autowired
    private CourseJPARepository courseJPARepository;
    @Autowired
    private UserJPARepository userJPARepository;


    @BeforeEach
    void init() {
        sectionProvider = new DBSectionProvider(sectionJPARepository);
        courseProvider = new DBCourseProvider(courseJPARepository, userJPARepository);
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
