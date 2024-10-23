package tech.zerofiltre.blog.domain.company;

import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;

import java.util.Optional;

public interface CompanyUserProvider {
    LinkCompanyUser link(LinkCompanyUser linkCompanyUser);

    Optional<LinkCompanyUser> findByCompanyIdAndUserId(long companyId, long userId);

    Page<LinkCompanyUser> findAllByCompanyId(int pageNumber, int pageSize, long companyId);

    void unlink(LinkCompanyUser linkCompanyUser);

    void unlinkAllByCompanyId(long companyId);

    void unlinkAllByCompanyIdExceptAdminRole(long companyId);

    void unlinkAllByUserId(long userId);

}
