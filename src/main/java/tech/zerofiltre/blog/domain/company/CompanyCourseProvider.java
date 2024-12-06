package tech.zerofiltre.blog.domain.company;

import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyCourse;

import java.util.List;
import java.util.Optional;

public interface CompanyCourseProvider {
    LinkCompanyCourse link(LinkCompanyCourse linkCompanyCourse);

    Optional<LinkCompanyCourse> linkOf(long companyId, long courseId);

    Optional<LinkCompanyCourse> linkOf(long companyId, long courseId, boolean active);

    Page<LinkCompanyCourse> findAllByCompanyIdByPage(int pageNumber, int pageSize, long companyId);

    List<LinkCompanyCourse> findAllByCompanyId(long companyId);

    void unlink(LinkCompanyCourse linkCompanyCourse);

    void unlinkAllByCompanyId(long companyId);

    void unlinkAllByCourseId(long courseId);
}
