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
import tech.zerofiltre.blog.util.ZerofiltreUtilsTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DBCompanyUserProviderIT {

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
    @DisplayName("given a LinkCompanyUser when save then return a LinkCompanyUser")
    void save() {
        //GIVEN
        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(0, 1, 1, LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null);

        //WHEN
        LinkCompanyUser response = dbCompanyUserProvider.save(linkCompanyUser);

        //THEN
        assertThat(response.getCompanyId()).isEqualTo(linkCompanyUser.getCompanyId());
        assertThat(response.getUserId()).isEqualTo(linkCompanyUser.getUserId());

    }

    @Test
    @DisplayName("given a companyId and userId and active LinkCompanyUser and ADMIN when findByCompanyIdAndUserId then return optional LinkCompanyUser")
    void findByCompanyIdAndUserId() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtilsTest.createMockUser(false));
        dbCompanyUserProvider.save(new LinkCompanyUser(0, company.getId(), user.getId(), LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null));

        //WHEN
        Optional<LinkCompanyUser> response = dbCompanyUserProvider.findByCompanyIdAndUserId(company.getId(), user.getId());

        //THEN
        assertThat(response).isPresent();
        assertThat(response.get().getCompanyId()).isEqualTo(company.getId());
        assertThat(response.get().getUserId()).isEqualTo(user.getId());
        assertThat(response.get().getRole()).isEqualTo(LinkCompanyUser.Role.ADMIN);
    }

    @Test
    @DisplayName("When search a active link between a company and a user, then verify that the link exists")
    void findByCompanyIdAndUserIdAndActiveIsTrue() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));
        User user = dbUserProvider.save(ZerofiltreUtilsTest.createMockUser(false));
        LinkCompanyUser linkCompanyUser = dbCompanyUserProvider.save(new LinkCompanyUser(0, company.getId(), user.getId(), LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null));

        assertThat(linkCompanyUser).isNotNull();

        //WHEN
        Optional<LinkCompanyUser> response = dbCompanyUserProvider.findByCompanyIdAndUserId(company.getId(), user.getId(), true);

        //THEN
        assertThat(response).isPresent();
        assertThat(response.get().getCompanyId()).isEqualTo(company.getId());
        assertThat(response.get().getUserId()).isEqualTo(user.getId());
        assertThat(response.get().getRole()).isEqualTo(LinkCompanyUser.Role.ADMIN);
    }

    @Test
    @DisplayName("given pageNumber, pageSize and companyId when get users then return page of LinkCompanyUser for a company")
    void findAllByCompanyId_byPage() {
        //GIVEN
        Company company = new Company(0, "Company1", "000000001");
        company = dbCompanyProvider.save(company);

        User user1 = ZerofiltreUtilsTest.createMockUser(false);
        user1 = dbUserProvider.save(user1);

        LinkCompanyUser linkCompanyUser1 = new LinkCompanyUser(0, company.getId(), user1.getId(), LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null);
        linkCompanyUser1 = dbCompanyUserProvider.save(linkCompanyUser1);

        User user2 = new User();
        user2.setEmail("user2@email.com");
        user2.setPseudoName("user2");
        user2 = dbUserProvider.save(user2);

        LinkCompanyUser linkCompanyUser2 = new LinkCompanyUser(0, company.getId(), user2.getId(), LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null);
        linkCompanyUser2 = dbCompanyUserProvider.save(linkCompanyUser2);

        //WHEN
        Page<LinkCompanyUser> response = dbCompanyUserProvider.findAllByCompanyId(0, 10, company.getId());

        //THEN
        assertThat(response).isNotNull();
        List<LinkCompanyUser> content = response.getContent();
        assertThat(content).hasSize(2);

        assertThat(content.get(0).getCompanyId()).isEqualTo(linkCompanyUser1.getCompanyId());
        assertThat(content.get(0).getUserId()).isEqualTo(linkCompanyUser1.getUserId());
        assertThat(content.get(0).getRole()).isEqualTo(linkCompanyUser1.getRole());
        assertThat(content.get(1).getCompanyId()).isEqualTo(linkCompanyUser2.getCompanyId());
        assertThat(content.get(1).getUserId()).isEqualTo(linkCompanyUser2.getUserId());
        assertThat(content.get(1).getRole()).isEqualTo(linkCompanyUser2.getRole());
    }

    @Test
    @DisplayName("When I search for all the links between users and a company, then I receive a list")
    void findAllByCompanyId() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));

        User user1 = dbUserProvider.save(new User());

        dbCompanyUserProvider.save(new LinkCompanyUser(0, company.getId(), user1.getId(), LinkCompanyUser.Role.VIEWER, true, LocalDateTime.now(), null));

        User user2 = dbUserProvider.save(new User());

        dbCompanyUserProvider.save(new LinkCompanyUser(0, company.getId(), user2.getId(), LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null));

        //WHEN
        List<LinkCompanyUser> response = dbCompanyUserProvider.findAllByCompanyId(company.getId());

        //THEN
        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getCompanyId()).isEqualTo(company.getId());
        assertThat(response.get(0).getUserId()).isEqualTo(user1.getId());
        assertThat(response.get(1).getCompanyId()).isEqualTo(company.getId());
        assertThat(response.get(1).getUserId()).isEqualTo(user2.getId());
    }

    @Test
    @DisplayName("When I search for all the links between users and a company except company admin, then I receive a list")
    void findAllByCompanyIdExceptAdminRole() {
        //GIVEN
        Company company = dbCompanyProvider.save(new Company(0, "Company1", "000000001"));

        User user = dbUserProvider.save(new User());

        dbCompanyUserProvider.save(new LinkCompanyUser(0, company.getId(), user.getId(), LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null));

        User user1 = dbUserProvider.save(new User());

        dbCompanyUserProvider.save(new LinkCompanyUser(0, company.getId(), user1.getId(), LinkCompanyUser.Role.VIEWER, true, LocalDateTime.now(), null));

        User user2 = dbUserProvider.save(new User());

        dbCompanyUserProvider.save(new LinkCompanyUser(0, company.getId(), user2.getId(), LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null));

        assertThat(dbCompanyUserProvider.findAllByCompanyId(company.getId()).size()).isEqualTo(3);

        //WHEN
        List<LinkCompanyUser> response = dbCompanyUserProvider.findAllByCompanyIdExceptAdminRole(company.getId());

        //THEN
        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getCompanyId()).isEqualTo(company.getId());
        assertThat(response.get(0).getUserId()).isEqualTo(user1.getId());
        assertThat(response.get(1).getCompanyId()).isEqualTo(company.getId());
        assertThat(response.get(1).getUserId()).isEqualTo(user2.getId());
    }

    @Test
    @DisplayName("given a LinkCompanyUser when delete then delete the LinkCompanyUser")
    void delete() {
        //GIVEN
        Company company = new Company(0, "Company1", "000000001");
        company = dbCompanyProvider.save(company);

        User user = ZerofiltreUtilsTest.createMockUser(false);
        user = dbUserProvider.save(user);

        LinkCompanyUser linkCompanyUser = dbCompanyUserProvider.save(new LinkCompanyUser(0, company.getId(), user.getId(), LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null));

        Optional<LinkCompanyUser> cu = dbCompanyUserProvider.findByCompanyIdAndUserId(linkCompanyUser.getCompanyId(), linkCompanyUser.getUserId(), true);
        assertThat(cu).isPresent();

        //WHEN
        dbCompanyUserProvider.delete(linkCompanyUser);

        //THEN
        cu = dbCompanyUserProvider.findByCompanyIdAndUserId(linkCompanyUser.getCompanyId(), linkCompanyUser.getUserId());

        assertThat(cu).isEmpty();
    }

    @Test
    @DisplayName("given a companyId when deleteAllByCompanyId then delete all LinkCompanyUser for a company")
    void deleteAllByCompanyId() {
        //GIVEN
        Company company = new Company(0, "Company1", "000000001");
        company = dbCompanyProvider.save(company);

        User user1 = ZerofiltreUtilsTest.createMockUser(false);
        user1 = dbUserProvider.save(user1);

        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(0, company.getId(), user1.getId(), LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null);
        dbCompanyUserProvider.save(linkCompanyUser);

        User user2 = new User();
        user2.setEmail("user2@email.com");
        user2.setPseudoName("user2");
        user2 = dbUserProvider.save(user2);

        linkCompanyUser = new LinkCompanyUser(0, company.getId(), user2.getId(), LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null);
        dbCompanyUserProvider.save(linkCompanyUser);

        //WHEN
        dbCompanyUserProvider.deleteAllByCompanyId(company.getId());

        //THEN
        Page<LinkCompanyUser> response = dbCompanyUserProvider.findAllByCompanyId(0, 10, company.getId());

        assertThat(response).isNotNull();
        List<LinkCompanyUser> content = response.getContent();
        assertThat(content).hasSize(0);
    }

    @Test
    @DisplayName("given a companyId when deleteAllByCompanyIdExceptAdminRole then unlink all LinkCompanyUser for a company except user with admin role")
    void deleteAllByCompanyIdExceptAdminRole() {
        //GIVEN
        Company company = new Company(0, "Company1", "000000001");
        company = dbCompanyProvider.save(company);

        User user1 = ZerofiltreUtilsTest.createMockUser(false);
        user1 = dbUserProvider.save(user1);

        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(0, company.getId(), user1.getId(), LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null);
        dbCompanyUserProvider.save(linkCompanyUser);

        User user2 = new User();
        user2.setEmail("user2@email.com");
        user2.setPseudoName("user2");
        user2 = dbUserProvider.save(user2);

        linkCompanyUser = new LinkCompanyUser(0, company.getId(), user2.getId(), LinkCompanyUser.Role.EDITOR, true, LocalDateTime.now(), null);
        dbCompanyUserProvider.save(linkCompanyUser);

        User user3 = new User();
        user3.setEmail("user3@email.com");
        user3.setPseudoName("user3");
        user3 = dbUserProvider.save(user3);

        linkCompanyUser = new LinkCompanyUser(0, company.getId(), user3.getId(), LinkCompanyUser.Role.VIEWER, true, LocalDateTime.now(), null);
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
    @DisplayName("given a userId when deleteAllByUserId then delete all LinkCompanyUser with userId for all companies")
    void deleteAllByUserId() {
        //GIVEN
        Company company1 = new Company(0, "Company1", "000000001");
        company1 = dbCompanyProvider.save(company1);

        User user = ZerofiltreUtilsTest.createMockUser(false);
        user = dbUserProvider.save(user);

        LinkCompanyUser linkCompanyUser = new LinkCompanyUser(0, company1.getId(), user.getId(), LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null);
        dbCompanyUserProvider.save(linkCompanyUser);

        Company company2 = new Company(0, "Company2", "000000002");
        company2 = dbCompanyProvider.save(company2);

        linkCompanyUser = new LinkCompanyUser(0, company2.getId(), user.getId(), LinkCompanyUser.Role.ADMIN, true, LocalDateTime.now(), null);
        dbCompanyUserProvider.save(linkCompanyUser);

        //WHEN
        dbCompanyUserProvider.deleteAllByUserId(user.getId());

        //THEN
        Page<LinkCompanyUser> response = dbCompanyUserProvider.findAllByCompanyId(0, 10, company1.getId());

        assertThat(response).isNotNull();
        List<LinkCompanyUser> content = response.getContent();
        assertThat(content).hasSize(0);

        Page<LinkCompanyUser> response2 = dbCompanyUserProvider.findAllByCompanyId(0, 10, company2.getId());

        assertThat(response2).isNotNull();
        List<LinkCompanyUser> content2 = response2.getContent();
        assertThat(content2).hasSize(0);
    }

}
