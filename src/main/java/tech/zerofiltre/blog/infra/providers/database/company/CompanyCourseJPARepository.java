package tech.zerofiltre.blog.infra.providers.database.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.infra.providers.database.company.model.LinkCompanyCourseJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.CourseJPA;

import java.util.List;
import java.util.Optional;

public interface CompanyCourseJPARepository extends JpaRepository<LinkCompanyCourseJPA, Long> {

    Optional<LinkCompanyCourseJPA> findByCompanyIdAndCourseId(long companyId, long courseId);

    Optional<LinkCompanyCourseJPA> findByCompanyIdAndCourseIdAndActive(long companyId, long courseId, boolean active);

    Page<LinkCompanyCourseJPA> findAllByCompanyId(Pageable pageable, long companyId);

    @Query("SELECT c FROM tech.zerofiltre.blog.infra.providers.database.course.model.CourseJPA c JOIN LinkCompanyCourseJPA l ON l.companyId = ?1 AND c.id = l.courseId WHERE c.status = ?2")
    Page<CourseJPA> findAllCoursesByCompanyId(Pageable pageable, long companyId, Status status);

    List<LinkCompanyCourseJPA> findAllByCompanyId(long companyId);

    List<LinkCompanyCourseJPA> findAllByCourseId(long courseId);

    @Modifying
    void deleteAllByCompanyId(long companyId);

    @Modifying
    void deleteAllByCourseId(long courseId);

}
