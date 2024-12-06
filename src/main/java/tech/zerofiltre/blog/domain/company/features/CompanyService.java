package tech.zerofiltre.blog.domain.company.features;

import lombok.RequiredArgsConstructor;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.features.IsAdminUser;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Optional;

@RequiredArgsConstructor
public class CompanyService {

    private final CompanyProvider companyProvider;
    private final CompanyUserProvider companyUserProvider;
    private final CompanyCourseProvider companyCourseProvider;
    private final IsAdminUser isAdminUser;
    private final IsCompanyExists isCompanyExists;
    private final IsAdminOrCompanyAdmin isAdminOrCompanyAdmin;

    public Company save(User user, Company company) throws ForbiddenActionException {
        isAdminUser.execute(user);

        return companyProvider.save(company);
    }

    public Company patch(User user, Company company) throws ForbiddenActionException, ResourceNotFoundException {
        isAdminOrCompanyAdmin.execute(user, company.getId());
        isCompanyExists.execute(company.getId());

        return companyProvider.save(company);
    }

    public Optional<Company> findById(User user, long id) throws ForbiddenActionException, ResourceNotFoundException {
        isAdminOrCompanyAdmin.execute(user, id);

        return companyProvider.findById(id);
    }

    public Page<Company> findAll(User user, int pageNumber, int pageSize) throws ForbiddenActionException {
        isAdminUser.execute(user);

        return companyProvider.findAll(pageNumber, pageSize);
    }

    public void delete(User user, Company company) throws ForbiddenActionException, ResourceNotFoundException {
        isAdminUser.execute(user);
        isCompanyExists.execute(company.getId());

        companyUserProvider.unlinkAllByCompanyId(company.getId());
        companyCourseProvider.unlinkAllByCompanyId(company.getId());
        companyProvider.delete(company);
    }

}
