package tech.zerofiltre.blog.infra.providers.database.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import tech.zerofiltre.blog.domain.company.model.UserCompanyInfos;
import tech.zerofiltre.blog.infra.providers.database.company.model.LinkCompanyUserJPA;

import java.util.List;
import java.util.Optional;

public interface CompanyUserJPARepository extends JpaRepository<LinkCompanyUserJPA, Long> {

    Optional<LinkCompanyUserJPA> findByCompanyIdAndUserId(long companyId, long userId);

    Optional<LinkCompanyUserJPA> findByCompanyIdAndUserIdAndActive(long companyId, long userId, boolean active);

    Page<LinkCompanyUserJPA> findAllByCompanyId(Pageable pageable, long companyId);

    List<LinkCompanyUserJPA> findAllByCompanyId(long companyId);

    @Query("select new tech.zerofiltre.blog.domain.company.model.UserCompanyInfos(l.companyId, l.role) from LinkCompanyUserJPA l where l.userId = :userId and  l.active = true ")
    List<UserCompanyInfos> findAllByUserId(long userId);

    List<LinkCompanyUserJPA> findAllByCompanyIdAndRoleNot(long companyId, String role);

    @Modifying
    void deleteAllByCompanyId(long companyId);

    @Modifying
    void deleteAllByCompanyIdAndRoleNot(long companyId, String role);

    @Modifying
    void deleteAllByUserId(long userId);

}
