package tech.zerofiltre.blog.infra.providers.database.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import tech.zerofiltre.blog.infra.providers.database.company.model.CompanyUserJPA;

import java.util.Optional;

public interface CompanyUserJPARepository extends JpaRepository<CompanyUserJPA, Long> {

    Optional<CompanyUserJPA> findByCompanyIdAndUserId(long companyId, long userId);

//    @Query("SELECT cc FROM CompanyUserJPA cc WHERE cc.company.id = ?1")
    Page<CompanyUserJPA> findAllByCompanyId(Pageable pageable, long companyId);

    @Modifying
//    @Query("DELETE FROM CompanyUserJPA cc WHERE cc.company.id = ?1")
    void deleteAllByCompanyId(long companyId);

    @Modifying
//    @Query("DELETE FROM CompanyUserJPA cc WHERE cc.company.id = ?1 AND cc.role <> 'ADMIN'")
    void deleteAllByCompanyIdAndRoleNot(long companyId, String role);

    @Modifying
//    @Query("DELETE FROM CompanyUserJPA cc WHERE cc.user.id = ?1")
    void deleteAllByUserId(long userId);

}
