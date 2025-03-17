package tech.zerofiltre.blog.infra.providers.database.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.zerofiltre.blog.infra.providers.database.company.model.CompanyJPA;

import java.util.List;

public interface CompanyJPARepository extends JpaRepository<CompanyJPA, Long> {

    @Query("SELECT c FROM CompanyJPA c JOIN LinkCompanyUserJPA l ON c.id = l.companyId AND l.userId = ?1")
    Page<CompanyJPA> findAllByUserId(Pageable pageable, long userId);

    @Query("SELECT c.id FROM CompanyJPA c " +
            "JOIN LinkCompanyUserJPA cu ON c.id = cu.companyId and cu.active = 1 and cu.userId = ?1 " +
            "JOIN LinkCompanyCourseJPA cc ON c.id = cc.companyId and cc.active = 1 and cc.courseId = ?2")
    List<Long> findAllCompanyIdByUserIdAndCourseId(long userId, long courseId);

}
