package tech.zerofiltre.blog.infra.providers.database.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.course.CourseJPARepository;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.database.user.UserJPARepository;
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DBCompanyCourseProviderIT {

    DBCompanyCourseProvider dbCompanyCourseProvider;

    DBCompanyProvider dbCompanyProvider;

    DBCourseProvider dbCourseProvider;

    DBUserProvider dbUserProvider;

    @Autowired
    CompanyCourseJPARepository companyCourseJPARepository;

    @Autowired
    CompanyJPARepository companyJPARepository;

    @Autowired
    CourseJPARepository courseJPARepository;

    @Autowired
    UserJPARepository userJPARepository;

    @BeforeEach
    void init() {
        dbCompanyCourseProvider = new DBCompanyCourseProvider(companyCourseJPARepository);
        dbCompanyProvider = new DBCompanyProvider(companyJPARepository);
        dbCourseProvider = new DBCourseProvider(courseJPARepository, userJPARepository);
        dbUserProvider = new DBUserProvider(userJPARepository);
    }

    @Test
    @DisplayName("given a companyCourse when save then return a LinkCompanyCourse")
    void save() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtilsTest.createMockUser(false));
        Course course = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));
        LinkCompanyCourse linkCompanyCourse = new LinkCompanyCourse(0, company.getId(), course.getId(), false, true, LocalDateTime.now(), null);

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
    @DisplayName("given a companyId and courseId when findByCompanyIdAndCourseId then return an optional LinkCompanyCourse")
    void findByCompanyIdAndCourseId() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtilsTest.createMockUser(false));
        Course course = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));
        LinkCompanyCourse linkCompanyCourse = dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course.getId(), false, false, LocalDateTime.now().minusDays(2), LocalDateTime.now()));

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
    @DisplayName("given a companyId and courseId and active LinkCompanyCourse and active is true when findByCompanyIdAndCourseId then return an optional LinkCompanyCourse")
    void findByCompanyIdAndCourseIdAndActiveIsTrue() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtilsTest.createMockUser(false));
        Course course = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));
        LinkCompanyCourse linkCompanyCourse = dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course.getId(), false, true, LocalDateTime.now(), null));

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
    @DisplayName("given pageNumber, pageSize and companyId when findAllByCompanyIdByPage then return page of LinkCompanyCourse for a company")
    void findAllByCompanyIdByPage_returnAllCompanyCourseByCompanyIdByPage() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtilsTest.createMockUser(false));
        Course course1 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        LinkCompanyCourse linkCompanyCourse1 = dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course1.getId(), false, false, LocalDateTime.now(), null));

        Course course2 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        LinkCompanyCourse linkCompanyCourse2 = dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course2.getId(), false, false, LocalDateTime.now(), null));

        //WHEN
        Page<LinkCompanyCourse> response = dbCompanyCourseProvider.findAllByCompanyIdByPage(0, 10, company.getId());

        //THEN
        assertThat(response).isNotNull();
        List<LinkCompanyCourse> content = response.getContent();
        assertThat(content).hasSize(2);

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
    @DisplayName("When I want to find the drafts of a company's courses, a list is returned.")
    void shouldReturnCoursesList_whenFindDraftCompanyCourse() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtilsTest.createMockUser(false));
        Course course1 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course1.getId(), false, false, LocalDateTime.now(), null));

        Course course2 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course2.getId(), false, false, LocalDateTime.now(), null));

        Course course3 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course3.getId(), false, false, LocalDateTime.now(), null));

        Course course4 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course4.getId(), false, false, LocalDateTime.now(), null));

        Course course5 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.IN_REVIEW, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course5.getId(), false, false, LocalDateTime.now(), null));

        Course course6 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.IN_REVIEW, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course6.getId(), false, false, LocalDateTime.now(), null));

        Course course7 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.ARCHIVED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course7.getId(), false, false, LocalDateTime.now(), null));

        Course course8 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.ARCHIVED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course8.getId(), false, false, LocalDateTime.now(), null));

        //WHEN
        Page<Course> response = dbCompanyCourseProvider.findAllCoursesByCompanyIdByPage(0, 10, company.getId(), Status.DRAFT);

        //THEN
        assertThat(response).isNotNull();
        List<Course> content = response.getContent();
        assertThat(content).hasSize(2);

        assertThat(content.get(0).getId()).isEqualTo(course1.getId());
        assertThat(content.get(0).getStatus()).isEqualTo(course1.getStatus());

        assertThat(content.get(1).getId()).isEqualTo(course2.getId());
        assertThat(content.get(1).getStatus()).isEqualTo(course2.getStatus());
    }

    @Test
    @DisplayName("When I want to find a company's published courses, a list is returned.")
    void shouldReturnCoursesList_whenFindCompanyPublishedCourse() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtilsTest.createMockUser(false));
        Course course1 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course1.getId(), false, false, LocalDateTime.now(), null));

        Course course2 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course2.getId(), false, false, LocalDateTime.now(), null));

        Course course3 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course3.getId(), false, false, LocalDateTime.now(), null));

        Course course4 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course4.getId(), false, false, LocalDateTime.now(), null));

        Course course5 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.IN_REVIEW, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course5.getId(), false, false, LocalDateTime.now(), null));

        Course course6 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.IN_REVIEW, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course6.getId(), false, false, LocalDateTime.now(), null));

        Course course7 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.ARCHIVED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course7.getId(), false, false, LocalDateTime.now(), null));

        Course course8 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.ARCHIVED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course8.getId(), false, false, LocalDateTime.now(), null));

        //WHEN
        Page<Course> response = dbCompanyCourseProvider.findAllCoursesByCompanyIdByPage(0, 10, company.getId(), Status.PUBLISHED);

        //THEN
        assertThat(response).isNotNull();
        List<Course> content = response.getContent();
        assertThat(content).hasSize(2);

        assertThat(content.get(0).getId()).isEqualTo(course3.getId());
        assertThat(content.get(0).getStatus()).isEqualTo(course3.getStatus());

        assertThat(content.get(1).getId()).isEqualTo(course4.getId());
        assertThat(content.get(1).getStatus()).isEqualTo(course4.getStatus());
    }

    @Test
    @DisplayName("When I want to find a company's review courses, a list is returned.")
    void shouldReturnCoursesList_whenFindCompanyInReviewCourse() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtilsTest.createMockUser(false));
        Course course1 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course1.getId(), false, false, LocalDateTime.now(), null));

        Course course2 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course2.getId(), false, false, LocalDateTime.now(), null));

        Course course3 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course3.getId(), false, false, LocalDateTime.now(), null));

        Course course4 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course4.getId(), false, false, LocalDateTime.now(), null));

        Course course5 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.IN_REVIEW, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course5.getId(), false, false, LocalDateTime.now(), null));

        Course course6 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.IN_REVIEW, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course6.getId(), false, false, LocalDateTime.now(), null));

        Course course7 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.ARCHIVED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course7.getId(), false, false, LocalDateTime.now(), null));

        Course course8 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.ARCHIVED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course8.getId(), false, false, LocalDateTime.now(), null));

        //WHEN
        Page<Course> response = dbCompanyCourseProvider.findAllCoursesByCompanyIdByPage(0, 10, company.getId(), Status.IN_REVIEW);

        //THEN
        assertThat(response).isNotNull();
        List<Course> content = response.getContent();
        assertThat(content).hasSize(2);

        assertThat(content.get(0).getId()).isEqualTo(course5.getId());
        assertThat(content.get(0).getStatus()).isEqualTo(course5.getStatus());

        assertThat(content.get(1).getId()).isEqualTo(course6.getId());
        assertThat(content.get(1).getStatus()).isEqualTo(course6.getStatus());
    }

    @Test
    @DisplayName("When I want to find a company's archived courses, a list is returned.")
    void shouldReturnCoursesList_whenFindCompanyArchivedCourse() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtilsTest.createMockUser(false));
        Course course1 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course1.getId(), false, false, LocalDateTime.now(), null));

        Course course2 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course2.getId(), false, false, LocalDateTime.now(), null));

        Course course3 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course3.getId(), false, false, LocalDateTime.now(), null));

        Course course4 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course4.getId(), false, false, LocalDateTime.now(), null));

        Course course5 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.IN_REVIEW, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course5.getId(), false, false, LocalDateTime.now(), null));

        Course course6 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.IN_REVIEW, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course6.getId(), false, false, LocalDateTime.now(), null));

        Course course7 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.ARCHIVED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course7.getId(), false, false, LocalDateTime.now(), null));

        Course course8 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.ARCHIVED, user, Collections.emptyList(), Collections.emptyList()));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course8.getId(), false, false, LocalDateTime.now(), null));

        //WHEN
        Page<Course> response = dbCompanyCourseProvider.findAllCoursesByCompanyIdByPage(0, 10, company.getId(), Status.ARCHIVED);

        //THEN
        assertThat(response).isNotNull();
        List<Course> content = response.getContent();
        assertThat(content).hasSize(2);

        assertThat(content.get(0).getId()).isEqualTo(course7.getId());
        assertThat(content.get(0).getStatus()).isEqualTo(course7.getStatus());

        assertThat(content.get(1).getId()).isEqualTo(course8.getId());
        assertThat(content.get(1).getStatus()).isEqualTo(course8.getStatus());
    }

    @Test
    @DisplayName("given companyId when findAllByCompanyId then return list of LinkCompanyCourse for a company")
    void findAllByCompanyId() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtilsTest.createMockUser(false));
        Course course1 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course1.getId(), false, true, LocalDateTime.now(), null));

        Course course2 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course2.getId(), false, true, LocalDateTime.now(), null));

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
    @DisplayName("given courseId when findAllByCourseId then return list of LinkCompanyCourse for a course")
    void findAllByCourseId() {
        //GIVEN
        User user = dbUserProvider.save(ZerofiltreUtilsTest.createMockUser(false));
        Course course = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        Company company1 = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company1.getId(), course.getId(), false, false, LocalDateTime.now().minusDays(2), LocalDateTime.now()));

        Company company2 = dbCompanyProvider.save(new Company(0, "Company2", "000000002"));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company2.getId(), course.getId(), false, true, LocalDateTime.now(), null));

        //WHEN
        List<LinkCompanyCourse> response = dbCompanyCourseProvider.findAllByCourseId(course.getId());

        //THEN
        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getCompanyId()).isEqualTo(company1.getId());
        assertThat(response.get(0).getCourseId()).isEqualTo(course.getId());
        assertThat(response.get(1).getCompanyId()).isEqualTo(company2.getId());
        assertThat(response.get(1).getCourseId()).isEqualTo(course.getId());
    }

    @Test
    @DisplayName("Given a LinkCompanyCourse when delete then delete the LinkCompanyCourse")
    void delete() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtilsTest.createMockUser(false));
        Course course1 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        LinkCompanyCourse linkCompanyCourse = dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course1.getId(), false, true, LocalDateTime.now(), null));

        Optional<LinkCompanyCourse> cc = dbCompanyCourseProvider.findByCompanyIdAndCourseId(linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId(), true);
        assertThat(cc).isPresent();

        //WHEN
        dbCompanyCourseProvider.delete(linkCompanyCourse);

        //THEN
        cc = dbCompanyCourseProvider.findByCompanyIdAndCourseId(linkCompanyCourse.getCompanyId(), linkCompanyCourse.getCourseId(), true);

        assertThat(cc).isEmpty();
    }

    @Test
    @DisplayName("Given a companyId when deleteAllByCompanyId then delete all LinkCompanyCourse for a company")
    void deleteAllByCompanyId() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtilsTest.createMockUser(false));
        Course course1 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course1.getId(), false, true, LocalDateTime.now(), null));

        Course course2 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course2.getId(), false, true, LocalDateTime.now(), null));

        Page<LinkCompanyCourse> response = dbCompanyCourseProvider.findAllByCompanyIdByPage(0, 10, company.getId());
        assertThat(response.getTotalNumberOfElements()).isEqualTo(2);

        //WHEN
        dbCompanyCourseProvider.deleteAllByCompanyId(company.getId());

        //THEN
        response = dbCompanyCourseProvider.findAllByCompanyIdByPage(0, 10, company.getId());

        assertThat(response).isNotNull();
        List<LinkCompanyCourse> content = response.getContent();
        assertThat(content).hasSize(0);
    }

    @Test
    @DisplayName("Given a courseId when deleteAllByCourseId then delete all LinkCompanyCourse with courseId for all companies")
    void deleteAllByCourseId() {
        //GIVEN
        Company company1 = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtilsTest.createMockUser(false));
        Course course1 = dbCourseProvider.save(ZerofiltreUtilsTest.createMockCourse(false, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList()));

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company1.getId(), course1.getId(), false, false, LocalDateTime.now().minusDays(2), LocalDateTime.now()));

        Company company2 = dbCompanyProvider.save(new Company(0, "Company2", "000000002"));

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company2.getId(), course1.getId(), false, true, LocalDateTime.now(), null));

        //WHEN
        dbCompanyCourseProvider.deleteAllByCourseId(course1.getId());

        //THEN
        Page<LinkCompanyCourse> response = dbCompanyCourseProvider.findAllByCompanyIdByPage(0, 10, company1.getId());

        assertThat(response).isNotNull();
        List<LinkCompanyCourse> content = response.getContent();
        assertThat(content).hasSize(0);

        Page<LinkCompanyCourse> response2 = dbCompanyCourseProvider.findAllByCompanyIdByPage(0, 10, company2.getId());

        assertThat(response2).isNotNull();
        List<LinkCompanyCourse> content2 = response2.getContent();
        assertThat(content2).hasSize(0);
    }

}
