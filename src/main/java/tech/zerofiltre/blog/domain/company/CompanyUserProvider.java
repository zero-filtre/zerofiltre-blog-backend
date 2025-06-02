package tech.zerofiltre.blog.domain.company;

import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.model.UserCompanyInfosVM;

import java.util.List;
import java.util.Optional;

public interface CompanyUserProvider {
    LinkCompanyUser save(LinkCompanyUser linkCompanyUser);

    Optional<LinkCompanyUser> findByCompanyIdAndUserId(long companyId, long userId);

    Optional<LinkCompanyUser> findByCompanyIdAndUserId(long companyId, long userId, boolean active);

    Page<LinkCompanyUser> findAllByCompanyId(int pageNumber, int pageSize, long companyId);

    List<LinkCompanyUser> findAllByCompanyId(long companyId);

    List<LinkCompanyUser> findAllByCompanyIdExceptAdminRole(long companyId);

    List<UserCompanyInfosVM> findCompaniesAndRolesByUserId(long userId);

    void delete(LinkCompanyUser linkCompanyUser);

    void deleteAllByCompanyId(long companyId);

    void deleteAllByCompanyIdExceptAdminRole(long companyId);

    void deleteAllByUserId(long userId);

}
