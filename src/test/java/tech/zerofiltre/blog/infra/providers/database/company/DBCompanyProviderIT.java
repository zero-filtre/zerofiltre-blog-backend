package tech.zerofiltre.blog.infra.providers.database.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.database.user.UserJPARepository;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DBCompanyProviderIT {

    DBCompanyProvider dbCompanyProvider;

    DBCompanyUserProvider dbCompanyUserProvider;

    DBUserProvider dbUserProvider;

    @Autowired
    CompanyJPARepository repository;

    @Autowired
    CompanyUserJPARepository companyUserJPARepository;

    @Autowired
    UserJPARepository userJPARepository;

    @BeforeEach
    void init() {
        dbCompanyProvider = new DBCompanyProvider(repository);
        dbCompanyUserProvider = new DBCompanyUserProvider(companyUserJPARepository);
        dbUserProvider = new DBUserProvider(userJPARepository);
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
        User user = ZerofiltreUtils.createMockUser(false);
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