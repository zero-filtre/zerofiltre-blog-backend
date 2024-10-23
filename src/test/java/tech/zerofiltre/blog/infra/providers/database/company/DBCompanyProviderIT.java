package tech.zerofiltre.blog.infra.providers.database.company;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.model.Company;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DBCompanyProviderIT {

    DBCompanyProvider dbCompanyProvider;

    @Autowired
    CompanyJPARepository repository;

    @BeforeEach
    void init() {
        dbCompanyProvider = new DBCompanyProvider(repository);
    }

    @Test
    @DisplayName("given company when save then return saved company")
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
    @DisplayName("given companyId when findById then return optional company")
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
        Assertions.assertThat(content).hasSize(2);

        assertThat(content.get(0).getId()).isEqualTo(company1.getId());
        assertThat(content.get(0).getCompanyName()).isEqualTo(company1.getCompanyName());
        assertThat(content.get(0).getSiren()).isEqualTo(company1.getSiren());

        assertThat(content.get(1).getId()).isEqualTo(company2.getId());
        assertThat(content.get(1).getCompanyName()).isEqualTo(company2.getCompanyName());
        assertThat(content.get(1).getSiren()).isEqualTo(company2.getSiren());
    }

    @Test
    @DisplayName("given company when delete then verify deleted company")
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