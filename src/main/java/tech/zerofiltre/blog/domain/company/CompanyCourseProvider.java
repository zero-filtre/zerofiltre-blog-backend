package tech.zerofiltre.blog.domain.company;

import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;
import tech.zerofiltre.blog.domain.course.model.Course;

import java.util.List;
import java.util.Optional;

public interface CompanyCourseProvider {
    LinkCompanyCourse save(LinkCompanyCourse linkCompanyCourse);

    Optional<LinkCompanyCourse> findByCompanyIdAndCourseId(long companyId, long courseId);

    Optional<LinkCompanyCourse> findByCompanyIdAndCourseId(long companyId, long courseId, boolean active);

    Page<LinkCompanyCourse> findAllByCompanyIdByPage(int pageNumber, int pageSize, long companyId);

    Page<Course> findAllCoursesByCompanyIdByPage(int pageNumber, int pageSize, long companyId);

    List<LinkCompanyCourse> findAllByCompanyId(long companyId);

    List<LinkCompanyCourse> findAllByCourseId(long courseId);

    void delete(LinkCompanyCourse linkCompanyCourse);

    void deleteAllByCompanyId(long companyId);

    void deleteAllByCourseId(long courseId);

}
