package tech.zerofiltre.blog.infra.providers.database.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.zerofiltre.blog.infra.providers.database.company.model.CompanyJPA;

public interface CompanyJPARepository extends JpaRepository<CompanyJPA, Long> {

    @Query("SELECT c FROM CompanyJPA c JOIN LinkCompanyUserJPA l ON c.id = l.companyId AND l.userId = ?1")
    Page<CompanyJPA> findAllByUserId(Pageable pageable, long userId);
}
