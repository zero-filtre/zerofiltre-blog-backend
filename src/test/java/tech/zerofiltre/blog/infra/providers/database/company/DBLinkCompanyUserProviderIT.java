package tech.zerofiltre.blog.infra.providers.database.company;

import org.assertj.core.api.Assertions;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class DBLinkCompanyUserProviderIT {

    DBCompanyUserProvider dbCompanyUserProvider;

    DBCompanyProvider dbCompanyProvider;

    DBUserProvider dbUserProvider;

    @Autowired
    CompanyUserJPARepository companyUserJPARepository;

    @Autowired
    CompanyJPARepository companyJPARepository;

    @Autowired
    UserJPARepository userJPARepository;

    @BeforeEach
    void init() {
        dbCompanyUserProvider = new DBCompanyUserProvider(companyUserJPARepository);
        dbCompanyProvider = new DBCompanyProvider(companyJPARepository);
        dbUserProvider = new DBUserProvider(userJPARepository);
    }

    @Test
    @DisplayName("given a companyUser when link then return a companyUser linked")
    void save() {
        //GIVEN
        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(1, 1, LinkCompanyUser.Role.ADMIN);

        //WHEN
        LinkCompanyUser response = dbCompanyUserProvider.save(linkCompanyUser);

        //THEN
        assertThat(response.getCompanyId()).isEqualTo(linkCompanyUser.getCompanyId());
        assertThat(response.getUserId()).isEqualTo(linkCompanyUser.getUserId());

    }

    @Test
    @DisplayName("given a companyId and userId and active companyUser and ROLE_COMPANY_ADMIN when findByCompanyIdAndUserId then return optional companyUser")
    void findByCompanyIdAndUserIdByCompanyIdAndUserId() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtils.createMockUser(false));
        dbCompanyUserProvider.save(new LinkCompanyUser(company.getId(), user.getId(), LinkCompanyUser.Role.ADMIN));

        //WHEN
        Optional<LinkCompanyUser> response = dbCompanyUserProvider.findByCompanyIdAndUserId(company.getId(), user.getId());

        //THEN
        assertThat(response).isPresent();
        assertThat(response.get().getCompanyId()).isEqualTo(company.getId());
        assertThat(response.get().getUserId()).isEqualTo(user.getId());
        assertThat(response.get().getRole()).isEqualTo(LinkCompanyUser.Role.ADMIN);
    }

    @Test
    @DisplayName("given pageNumber, pageSize and companyId when get users then return all users for a company")
    void findAllByCompanyId() {
        //GIVEN
        Company company = new Company(0, "Company1", "000000001");
        company = dbCompanyProvider.save(company);

        User user1 = ZerofiltreUtils.createMockUser(false);
        user1 = dbUserProvider.save(user1);

        LinkCompanyUser linkCompanyUser1 = new LinkCompanyUser(company.getId(), user1.getId(), LinkCompanyUser.Role.ADMIN);
        linkCompanyUser1 = dbCompanyUserProvider.save(linkCompanyUser1);

        User user2 = new User();
        user2.setEmail("user2@email.com");
        user2.setPseudoName("user2");
        user2 = dbUserProvider.save(user2);

        LinkCompanyUser linkCompanyUser2 = new LinkCompanyUser(company.getId(), user2.getId(), LinkCompanyUser.Role.ADMIN);
        linkCompanyUser2 = dbCompanyUserProvider.save(linkCompanyUser2);

        //WHEN
        Page<LinkCompanyUser> response = dbCompanyUserProvider.findAllByCompanyId(0, 10, company.getId());

        //THEN
        assertThat(response).isNotNull();
        List<LinkCompanyUser> content = response.getContent();
        Assertions.assertThat(content).hasSize(2);

        assertThat(content.get(0).getCompanyId()).isEqualTo(linkCompanyUser1.getCompanyId());
        assertThat(content.get(0).getUserId()).isEqualTo(linkCompanyUser1.getUserId());
        assertThat(content.get(0).getRole()).isEqualTo(linkCompanyUser1.getRole());
        assertThat(content.get(1).getCompanyId()).isEqualTo(linkCompanyUser2.getCompanyId());
        assertThat(content.get(1).getUserId()).isEqualTo(linkCompanyUser2.getUserId());
        assertThat(content.get(1).getRole()).isEqualTo(linkCompanyUser2.getRole());
    }

    @Test
    @DisplayName("given a companyUser when unlink then unlink the companyUser")
    void delete() {
        //GIVEN
        Company company = new Company(0, "Company1", "000000001");
        company = dbCompanyProvider.save(company);

        User user = ZerofiltreUtils.createMockUser(false);
        user = dbUserProvider.save(user);

        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(company.getId(), user.getId(), LinkCompanyUser.Role.ADMIN);
        linkCompanyUser = dbCompanyUserProvider.save(linkCompanyUser);

        //WHEN
        dbCompanyUserProvider.delete(linkCompanyUser);

        //THEN
        Optional<LinkCompanyUser> response = dbCompanyUserProvider.findByCompanyIdAndUserId(linkCompanyUser.getCompanyId(), linkCompanyUser.getUserId());

        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("given a companyId when unlinkAllByCompanyId then unlink all users for a company")
    void deleteAllByCompanyId() {
        //GIVEN
        Company company = new Company(0, "Company1", "000000001");
        company = dbCompanyProvider.save(company);

        User user1 = ZerofiltreUtils.createMockUser(false);
        user1 = dbUserProvider.save(user1);

        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(company.getId(), user1.getId(), LinkCompanyUser.Role.ADMIN);
        dbCompanyUserProvider.save(linkCompanyUser);

        User user2 = new User();
        user2.setEmail("user2@email.com");
        user2.setPseudoName("user2");
        user2 = dbUserProvider.save(user2);

        linkCompanyUser = new LinkCompanyUser(company.getId(), user2.getId(), LinkCompanyUser.Role.ADMIN);
        dbCompanyUserProvider.save(linkCompanyUser);

        //WHEN
        dbCompanyUserProvider.deleteAllByCompanyId(company.getId());

        //THEN
        Page<LinkCompanyUser> response = dbCompanyUserProvider.findAllByCompanyId(0, 10, company.getId());

        assertThat(response).isNotNull();
        List<LinkCompanyUser> content = response.getContent();
        Assertions.assertThat(content).hasSize(0);
    }

    @Test
    @DisplayName("given a companyId when unlinkAllByCompanyIdExceptAdminRole then unlink all users for a company except user with admin role")
    void deleteAllByCompanyIdExceptAdminRole() {
        //GIVEN
        Company company = new Company(0, "Company1", "000000001");
        company = dbCompanyProvider.save(company);

        User user1 = ZerofiltreUtils.createMockUser(false);
        user1 = dbUserProvider.save(user1);

        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(company.getId(), user1.getId(), LinkCompanyUser.Role.ADMIN);
        dbCompanyUserProvider.save(linkCompanyUser);

        User user2 = new User();
        user2.setEmail("user2@email.com");
        user2.setPseudoName("user2");
        user2 = dbUserProvider.save(user2);

        linkCompanyUser = new LinkCompanyUser(company.getId(), user2.getId(), LinkCompanyUser.Role.EDITOR);
        dbCompanyUserProvider.save(linkCompanyUser);

        User user3 = new User();
        user3.setEmail("user3@email.com");
        user3.setPseudoName("user3");
        user3 = dbUserProvider.save(user3);

        linkCompanyUser = new LinkCompanyUser(company.getId(), user3.getId(), LinkCompanyUser.Role.VIEWER);
        dbCompanyUserProvider.save(linkCompanyUser);

        //WHEN
        dbCompanyUserProvider.deleteAllByCompanyIdExceptAdminRole(company.getId());

        //THEN
        Optional<LinkCompanyUser> response = dbCompanyUserProvider.findByCompanyIdAndUserId(company.getId(), user1.getId());
        assertThat(response).isPresent();
        assertThat(response.get().getRole()).isEqualTo(LinkCompanyUser.Role.ADMIN);

        response = dbCompanyUserProvider.findByCompanyIdAndUserId(company.getId(), user2.getId());
        assertThat(response).isEmpty();

        response = dbCompanyUserProvider.findByCompanyIdAndUserId(company.getId(), user3.getId());
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("given a userId when unlinkAllByUserId then unlink all companyUser with userId for all companies")
    void deleteAllByUserId() {
        //GIVEN
        Company company1 = new Company(0, "Company1", "000000001");
        company1 = dbCompanyProvider.save(company1);

        User user = ZerofiltreUtils.createMockUser(false);
        user = dbUserProvider.save(user);

        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(company1.getId(), user.getId(), LinkCompanyUser.Role.ADMIN);
        dbCompanyUserProvider.save(linkCompanyUser);

        Company company2 = new Company(0, "Company2", "000000002");
        company2 = dbCompanyProvider.save(company2);

        linkCompanyUser = new LinkCompanyUser(company2.getId(), user.getId(), LinkCompanyUser.Role.ADMIN);
        dbCompanyUserProvider.save(linkCompanyUser);

        //WHEN
        dbCompanyUserProvider.deleteAllByUserId(user.getId());

        //THEN
        Page<LinkCompanyUser> response = dbCompanyUserProvider.findAllByCompanyId(0, 10, company1.getId());

        assertThat(response).isNotNull();
        List<LinkCompanyUser> content = response.getContent();
        Assertions.assertThat(content).hasSize(0);

        Page<LinkCompanyUser> response2 = dbCompanyUserProvider.findAllByCompanyId(0, 10, company2.getId());

        assertThat(response2).isNotNull();
        List<LinkCompanyUser> content2 = response2.getContent();
        Assertions.assertThat(content2).hasSize(0);
    }

}
