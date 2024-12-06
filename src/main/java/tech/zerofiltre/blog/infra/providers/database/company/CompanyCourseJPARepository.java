package tech.zerofiltre.blog.infra.providers.database.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import tech.zerofiltre.blog.infra.providers.database.company.model.CompanyCourseJPA;

import java.util.List;
import java.util.Optional;

public interface CompanyCourseJPARepository extends JpaRepository<CompanyCourseJPA, Long> {

    Optional<CompanyCourseJPA> findByCompanyIdAndCourseId(long companyId, long courseId);

    Optional<CompanyCourseJPA> findByCompanyIdAndCourseIdAndActive(long companyId, long courseId, boolean active);

    Page<CompanyCourseJPA> findAllByCompanyId(Pageable pageable, long companyId);

    List<CompanyCourseJPA> findAllByCompanyId(long companyId);

    @Modifying
    void deleteAllByCompanyId(long companyId);

    @Modifying
    void deleteAllByCourseId(long courseId);

}
