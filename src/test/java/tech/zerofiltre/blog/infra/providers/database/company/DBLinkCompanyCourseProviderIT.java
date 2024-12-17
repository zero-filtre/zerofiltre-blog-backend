package tech.zerofiltre.blog.infra.providers.database.company;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import({DBCompanyProvider.class, DBCourseProvider.class, DBUserProvider.class})
class DBLinkCompanyCourseProviderIT {

    DBCompanyCourseProvider dbCompanyCourseProvider;

    @Autowired
    DBCompanyProvider dbCompanyProvider;

    @Autowired
    DBCourseProvider dbCourseProvider;

    @Autowired
    DBUserProvider dbUserProvider;

    @Autowired
    CompanyCourseJPARepository companyCourseJPARepository;

    @BeforeEach
    void init() {
        dbCompanyCourseProvider = new DBCompanyCourseProvider(companyCourseJPARepository);
    }

    @Test
    @DisplayName("given a companyCourse when link then return a companyCourse linked")
    void save() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtils.createMockUser(false));
        Course course = dbCourseProvider.save(ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(0, company.getId(), course.getId(), true, LocalDateTime.now(), null);

        //WHEN
        LinkCompanyCourse response = dbCompanyCourseProvider.save(linkCompanyCourse);

        //THEN
        assertThat(response.getCompanyId()).isEqualTo(linkCompanyCourse.getCompanyId());
        assertThat(response.getCourseId()).isEqualTo(linkCompanyCourse.getCourseId());
        assertThat(response.isActive()).isTrue();
        assertThat(response.getLinkedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(response.getSuspendedAt()).isNull();
    }

    @Test
    @DisplayName("given a companyId and courseId when linkOf then return an optional companyCourse")
    void saveOfByCompanyIdAndCourseId() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtils.createMockUser(false));
        Course course = dbCourseProvider.save(ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));
        LinkCompanyCourse linkCompanyCourse = dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course.getId(), false, LocalDateTime.now().minusDays(2), LocalDateTime.now()));

        assertThat(linkCompanyCourse).isNotNull();

        //WHEN
        Optional<LinkCompanyCourse> response = dbCompanyCourseProvider.findByCompanyIdAndCourseId(company.getId(), course.getId());

        //THEN
        assertThat(response).isPresent();
        assertThat(response.get().getCompanyId()).isEqualTo(company.getId());
        assertThat(response.get().getCourseId()).isEqualTo(course.getId());
        assertThat(response.get().isActive()).isEqualTo(linkCompanyCourse.isActive());
        assertThat(response.get().getLinkedAt()).isEqualTo(linkCompanyCourse.getLinkedAt());
        assertThat(response.get().getSuspendedAt()).isEqualTo(linkCompanyCourse.getSuspendedAt());
    }

    @Test
    @DisplayName("given a companyId and courseId and active companyCourse and active is true when linkOf then return an optional companyCourse")
    void saveOfByCompanyIdAndCourseIdAndActiveIsTrue() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtils.createMockUser(false));
        Course course = dbCourseProvider.save(ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));
        LinkCompanyCourse linkCompanyCourse = dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course.getId(), true, LocalDateTime.now(), null));

        assertThat(linkCompanyCourse).isNotNull();

        //WHEN
        Optional<LinkCompanyCourse> response = dbCompanyCourseProvider.findByCompanyIdAndCourseId(company.getId(), course.getId(), true);

        //THEN
        assertThat(response).isPresent();
        assertThat(response.get().getCompanyId()).isEqualTo(company.getId());
        assertThat(response.get().getCourseId()).isEqualTo(course.getId());
        assertThat(response.get().isActive()).isEqualTo(linkCompanyCourse.isActive());
        assertThat(response.get().getLinkedAt()).isEqualTo(linkCompanyCourse.getLinkedAt());
        assertThat(response.get().getSuspendedAt()).isEqualTo(linkCompanyCourse.getSuspendedAt());
    }

    @Test
    @DisplayName("given pageNumber, pageSize and companyId when findAllByCompanyIdByPage then return all courses for a company")
    void findAllByCompanyId_returnAllCompanyCourseByCompanyId() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtils.createMockUser(false));
        Course course1 = dbCourseProvider.save(ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        LinkCompanyCourse linkCompanyCourse1 = dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course1.getId(), false, LocalDateTime.now(), null));

        Course course2 = dbCourseProvider.save(ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        LinkCompanyCourse linkCompanyCourse2 = dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course2.getId(), false, LocalDateTime.now(), null));

        //WHEN
        Page<LinkCompanyCourse> response = dbCompanyCourseProvider.findAllByCompanyIdByPage(0, 10, company.getId());

        //THEN
        assertThat(response).isNotNull();
        List<LinkCompanyCourse> content = response.getContent();
        Assertions.assertThat(content).hasSize(2);

        assertThat(content.get(0).getCompanyId()).isEqualTo(linkCompanyCourse1.getCompanyId());
        assertThat(content.get(0).getCourseId()).isEqualTo(linkCompanyCourse1.getCourseId());
        assertThat(content.get(0).isActive()).isEqualTo(linkCompanyCourse1.isActive());
        assertThat(content.get(0).getLinkedAt()).isEqualTo(linkCompanyCourse1.getLinkedAt());
        assertThat(content.get(0).getSuspendedAt()).isEqualTo(linkCompanyCourse1.getSuspendedAt());

        assertThat(content.get(1).getCompanyId()).isEqualTo(linkCompanyCourse2.getCompanyId());
        assertThat(content.get(1).getCourseId()).isEqualTo(linkCompanyCourse2.getCourseId());
        assertThat(content.get(1).isActive()).isEqualTo(linkCompanyCourse2.isActive());
        assertThat(content.get(1).getLinkedAt()).isEqualTo(linkCompanyCourse2.getLinkedAt());
        assertThat(content.get(1).getSuspendedAt()).isEqualTo(linkCompanyCourse2.getSuspendedAt());
    }

    @Test
    @DisplayName("given companyId when findAllByCompanyId then return list of companyCourse for a company")
    void findAllByCompanyId() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtils.createMockUser(false));
        Course course1 = dbCourseProvider.save(ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course1.getId(), true, LocalDateTime.now(), null));

        Course course2 = dbCourseProvider.save(ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course2.getId(), true, LocalDateTime.now(), null));

        //WHEN
        List<LinkCompanyCourse> response = dbCompanyCourseProvider.findAllByCompanyId(company.getId());

        //THEN
        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getCompanyId()).isEqualTo(company.getId());
        assertThat(response.get(0).getCourseId()).isEqualTo(course1.getId());
        assertThat(response.get(1).getCompanyId()).isEqualTo(company.getId());
        assertThat(response.get(1).getCourseId()).isEqualTo(course2.getId());
    }

    @Test
    @DisplayName("Given a companyCourse when unlink then unlink the companyCourse")
    void delete() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtils.createMockUser(false));
        Course course1 = dbCourseProvider.save(ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        LinkCompanyCourse linkCompanyCourse = dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course1.getId(), true, LocalDateTime.now(), null));

        Optional<LinkCompanyCourse> cc = dbCompanyCourseProvider.findByCompanyIdAndCourseId(linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId(), true);
        assertThat(cc).isPresent();

        //WHEN
        dbCompanyCourseProvider.delete(linkCompanyCourse);

        //THEN
        cc = dbCompanyCourseProvider.findByCompanyIdAndCourseId(linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId(), true);

        assertThat(cc).isEmpty();
    }

    @Test
    @DisplayName("Given a companyId when unlinkAllByCompanyId then unlink all courses for a company")
    void deleteAllByCompanyId() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtils.createMockUser(false));
        Course course1 = dbCourseProvider.save(ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course1.getId(), true, LocalDateTime.now(), null));

        Course course2 = dbCourseProvider.save(ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course2.getId(), true, LocalDateTime.now(), null));

        Page<LinkCompanyCourse> response = dbCompanyCourseProvider.findAllByCompanyIdByPage(0, 10, company.getId());
        Assertions.assertThat(response.getTotalNumberOfElements()).isEqualTo(2);

        //WHEN
        dbCompanyCourseProvider.deleteAllByCompanyId(company.getId());

        //THEN
        response = dbCompanyCourseProvider.findAllByCompanyIdByPage(0, 10, company.getId());

        assertThat(response).isNotNull();
        List<LinkCompanyCourse> content = response.getContent();
        Assertions.assertThat(content).hasSize(0);
    }

    @Test
    @DisplayName("Given a courseId when unlinkAllByCourseId then unlink all companyCourse with courseId for all companies")
    void deleteAllByCourseId() {
        //GIVEN
        Company company1 = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtils.createMockUser(false));
        Course course1 = dbCourseProvider.save(ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company1.getId(), course1.getId(), false, LocalDateTime.now().minusDays(2), LocalDateTime.now()));

        Company company2 = dbCompanyProvider.save(new Company(0, "Company2", "000000002"));

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company2.getId(), course1.getId(), true, LocalDateTime.now(), null));

        //WHEN
        dbCompanyCourseProvider.deleteAllByCourseId(course1.getId());

        //THEN
        Page<LinkCompanyCourse> response = dbCompanyCourseProvider.findAllByCompanyIdByPage(0, 10, company1.getId());

        assertThat(response).isNotNull();
        List<LinkCompanyCourse> content = response.getContent();
        Assertions.assertThat(content).hasSize(0);

        Page<LinkCompanyCourse> response2 = dbCompanyCourseProvider.findAllByCompanyIdByPage(0, 10, company2.getId());

        assertThat(response2).isNotNull();
        List<LinkCompanyCourse> content2 = response2.getContent();
        Assertions.assertThat(content2).hasSize(0);
    }

}
