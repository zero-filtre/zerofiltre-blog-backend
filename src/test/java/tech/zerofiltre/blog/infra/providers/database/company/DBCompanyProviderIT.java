package tech.zerofiltre.blog.infra.providers.database.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.course.CourseJPARepository;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.database.user.UserJPARepository;
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DBCompanyProviderIT {

    DBCompanyProvider dbCompanyProvider;
    DBCompanyUserProvider dbCompanyUserProvider;
    DBCompanyCourseProvider dbCompanyCourseProvider;

    DBUserProvider dbUserProvider;
    DBCourseProvider dbCourseProvider;

    @Autowired
    CompanyJPARepository repository;

    @Autowired
    CompanyUserJPARepository companyUserJPARepository;

    @Autowired
    CompanyCourseJPARepository companyCourseJPARepository;

    @Autowired
    UserJPARepository userJPARepository;

    @Autowired
    CourseJPARepository courseJPARepository;

    @BeforeEach
    void init() {
        dbCompanyProvider = new DBCompanyProvider(repository);

        dbCompanyUserProvider = new DBCompanyUserProvider(companyUserJPARepository);
        dbCompanyCourseProvider = new DBCompanyCourseProvider(companyCourseJPARepository);

        dbUserProvider = new DBUserProvider(userJPARepository);
        dbCourseProvider = new DBCourseProvider(courseJPARepository, userJPARepository);
    }

    @Test
    @DisplayName("given Company when save then return saved Company")
    void save() {
        //GIVEN
        Company company = new Company(0, "Company 1", "000000001");

        //WHEN
        Company response = dbCompanyProvider.save(company);

        //THEN
        assertThat(response.getCompanyName()).isEqualTo(company.getCompanyName());
        assertThat(response.getSiren()).isEqualTo(company.getSiren());
    }

    @Test
    @DisplayName("given companyId when findById then return optional Company")
    void findById() {
        //GIVEN
        Company company = new Company(0, "Company 1", "000000001");

        company = dbCompanyProvider.save(company);

        //WHEN
        Optional<Company> response = dbCompanyProvider.findById(company.getId());

        //THEN
        assertThat(response).isNotEmpty();
        assertThat(response.get().getId()).isEqualTo(company.getId());
        assertThat(response.get().getCompanyName()).isEqualTo(company.getCompanyName());
        assertThat(response.get().getSiren()).isEqualTo(company.getSiren());
    }

    @Test
    @DisplayName("given pageNumber, pageSize and companyId when findAll then return all companies")
    void findAll_returnAllCompanies() {
        //GIVEN
        Company company1 = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));

        Company company2 = dbCompanyProvider.save(new Company(0, "Company2", "000000002"));

        //WHEN
        Page<Company> response = dbCompanyProvider.findAll(0, 10);

        //THEN
        assertThat(response).isNotNull();
        List<Company> content = response.getContent();
        assertThat(content).hasSize(2);

        assertThat(content.get(0).getId()).isEqualTo(company1.getId());
        assertThat(content.get(0).getCompanyName()).isEqualTo(company1.getCompanyName());
        assertThat(content.get(0).getSiren()).isEqualTo(company1.getSiren());

        assertThat(content.get(1).getId()).isEqualTo(company2.getId());
        assertThat(content.get(1).getCompanyName()).isEqualTo(company2.getCompanyName());
        assertThat(content.get(1).getSiren()).isEqualTo(company2.getSiren());
    }

    @Test
    @DisplayName("given userId when findAllByUserId then return page Company by userId")
    void findAllByUserId() {
        //GIVEN
        User user = ZerofiltreUtilsTest.createMockUser(false);
        user = dbUserProvider.save(user);

        Company company1 = new Company(0, "Company1", "000000001");
        company1 = dbCompanyProvider.save(company1);

        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(0, company1.getId(), user.getId(), LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null);
        dbCompanyUserProvider.save(linkCompanyUser);

        Company company2 = new Company(0, "Company2", "000000002");
        company2 = dbCompanyProvider.save(company2);

        linkCompanyUser = new LinkCompanyUser(0, company2.getId(), user.getId(), LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null);
        dbCompanyUserProvider.save(linkCompanyUser);

        //WHEN
        Page<Company> response = dbCompanyProvider.findAllByUserId(0, 10, user.getId());

        //THEN
        assertThat(response).isNotNull();
        List<Company> content = response.getContent();
        assertThat(content).hasSize(2);
        assertThat(content.get(0).getId()).isEqualTo(company1.getId());
        assertThat(content.get(1).getId()).isEqualTo(company2.getId());
    }

    @Test
    @DisplayName("When I search for companies that have a user and a course, I get a list")
    void shouldGetList_whenFindAllCompaniesWithUserIdAndCourseId() {
        //GIVEN
        Company company1 = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        Company company2 = dbCompanyProvider.save(new Company(0, "Company2", "000000002"));
        Company company3 = dbCompanyProvider.save(new Company(0, "Company3", "000000003"));
        Company company4 = dbCompanyProvider.save(new Company(0, "Company4", "000000004"));

        User user1 = dbUserProvider.save(new User());

        dbCompanyUserProvider.save(new LinkCompanyUser(0, company2.getId(), user1.getId(), LinkCompanyUser.Role.VIEWER, true, LocalDateTime.now(), null));
        dbCompanyUserProvider.save(new LinkCompanyUser(0, company4.getId(), user1.getId(), LinkCompanyUser.Role.VIEWER, true, LocalDateTime.now(), null));

        User user2 = dbUserProvider.save(new User());

        dbCompanyUserProvider.save(new LinkCompanyUser(0, company1.getId(), user2.getId(), LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null));
        dbCompanyUserProvider.save(new LinkCompanyUser(0, company2.getId(), user2.getId(), LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null));
        dbCompanyUserProvider.save(new LinkCompanyUser(0, company3.getId(), user2.getId(), LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null));

        Course course1 = dbCourseProvider.save(new Course());

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company1.getId(), course1.getId(), true, LocalDateTime.now(), null));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company2.getId(), course1.getId(), true, LocalDateTime.now(), null));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company3.getId(), course1.getId(), true, LocalDateTime.now(), null));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company4.getId(), course1.getId(), true, LocalDateTime.now(), null));

        Course course2 = dbCourseProvider.save(new Course());

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company2.getId(), course2.getId(), true, LocalDateTime.now(), null));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company4.getId(), course2.getId(), true, LocalDateTime.now(), null));

        //WHEN
        List<Long> response = dbCompanyProvider.findAllCompanyIdByUserIdAndCourseId(user1.getId(), course1.getId());

        //THEN
        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0)).isEqualTo(company2.getId());
        assertThat(response.get(1)).isEqualTo(company4.getId());
    }

    @Test
    @DisplayName("When I search for companies with a user and a course, I get an empty list")
    void shouldGetEmptyList_whenFindAllCompaniesWithUserIdAndCourseId() {
        //GIVEN
        Company company1 = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        Company company2 = dbCompanyProvider.save(new Company(0, "Company2", "000000002"));
        Company company3 = dbCompanyProvider.save(new Company(0, "Company3", "000000003"));
        Company company4 = dbCompanyProvider.save(new Company(0, "Company4", "000000004"));

        User user1 = dbUserProvider.save(new User());

        User user2 = dbUserProvider.save(new User());

        dbCompanyUserProvider.save(new LinkCompanyUser(0, company1.getId(), user2.getId(), LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null));
        dbCompanyUserProvider.save(new LinkCompanyUser(0, company2.getId(), user2.getId(), LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null));
        dbCompanyUserProvider.save(new LinkCompanyUser(0, company3.getId(), user2.getId(), LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null));

        Course course1 = dbCourseProvider.save(new Course());

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company1.getId(), course1.getId(), true, LocalDateTime.now(), null));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company2.getId(), course1.getId(), true, LocalDateTime.now(), null));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company3.getId(), course1.getId(), true, LocalDateTime.now(), null));
        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company4.getId(), course1.getId(), true, LocalDateTime.now(), null));

        dbCourseProvider.save(new Course());

        //WHEN
        List<Long> response = dbCompanyProvider.findAllCompanyIdByUserIdAndCourseId(user1.getId(), course1.getId());

        //THEN
        assertThat(response).isNotNull();
        assertThat(response.size()).isZero();
    }

    @Test
    @DisplayName("When I search for companies that have a user and a course and the link between the company and the user is inactive, I get an empty list")
    void shouldGetEmptyList_whenFindAllCompaniesWithNotActiveUserAndCourse() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));

        User user = dbUserProvider.save(new User());

        dbCompanyUserProvider.save(new LinkCompanyUser(0, company.getId(), user.getId(), LinkCompanyUser.Role.EDITOR, false, LocalDateTime.now(), LocalDateTime.now()));

        Course course = dbCourseProvider.save(new Course());

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course.getId(), true, LocalDateTime.now(), null));

        //WHEN
        List<Long> response = dbCompanyProvider.findAllCompanyIdByUserIdAndCourseId(user.getId(), course.getId());

        //THEN
        assertThat(response).isNotNull();
        assertThat(response.size()).isZero();
    }

    @Test
    @DisplayName("When I search for companies that have a user and a course and the link between the company and the course is inactive, I get an empty list")
    void shouldGetEmptyList_whenFindAllCompaniesWithUserAndNotActiveCourse() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));

        User user = dbUserProvider.save(new User());

        dbCompanyUserProvider.save(new LinkCompanyUser(0, company.getId(), user.getId(), LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null));

        Course course = dbCourseProvider.save(new Course());

        dbCompanyCourseProvider.save(new LinkCompanyCourse(0, company.getId(), course.getId(), false, LocalDateTime.now(), LocalDateTime.now()));

        //WHEN
        List<Long> response = dbCompanyProvider.findAllCompanyIdByUserIdAndCourseId(user.getId(), course.getId());

        //THEN
        assertThat(response).isNotNull();
        assertThat(response.size()).isZero();
    }

    @Test
    @DisplayName("given Company when delete then verify deleted Company")
    void delete() {
        //GIVEN
        Company company = new Company(0, "Company 1", "000000001");

        company = dbCompanyProvider.save(company);

        //WHEN
        dbCompanyProvider.delete(company);

        //THEN
        assertThat(dbCompanyProvider.findById(company.getId())).isEmpty();
    }

}