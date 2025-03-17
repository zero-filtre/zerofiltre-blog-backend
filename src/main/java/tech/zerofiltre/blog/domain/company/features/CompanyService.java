package tech.zerofiltre.blog.domain.company.features;

import lombok.RequiredArgsConstructor;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.CompanyCourseProvider;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.DataChecker;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CompanyService {

    private final CompanyProvider companyProvider;
    private final CompanyUserProvider companyUserProvider;
    private final CompanyCourseProvider companyCourseProvider;
    private final DataChecker checker;

    public Company save(User user, Company company) throws ForbiddenActionException {
        checker.isAdminUser(user);

        return companyProvider.save(company);
    }

    public Company patch(User user, Company company) throws ForbiddenActionException, ResourceNotFoundException {
        checker.isAdminOrCompanyAdmin(user, company.getId());
        checker.companyExists(company.getId());

        return companyProvider.save(company);
    }

    public Optional<Company> findById(User user, long id) throws ForbiddenActionException {
        checker.isAdminOrCompanyAdmin(user, id);

        return companyProvider.findById(id);
    }

    public Page<Company> findAll(int pageNumber, int pageSize, User executor, long userId) throws ForbiddenActionException, ResourceNotFoundException {
        if(userId == 0) {
            if(executor.isAdmin()) {
                return companyProvider.findAll(pageNumber, pageSize);
            }
            return companyProvider.findAllByUserId(pageNumber, pageSize, executor.getId());
        }
        checker.isAdminUser(executor);
        checker.userExists(userId);
        return companyProvider.findAllByUserId(pageNumber, pageSize, userId);
    }

    public List<Long> findAllCompanyIdByUserIdAndCourseId(long userId, long courseId) {
        return companyProvider.findAllCompanyIdByUserIdAndCourseId(userId, courseId);
    }

    public void delete(User user, Company company) throws ForbiddenActionException, ResourceNotFoundException {
        checker.isAdminUser(user);
        checker.companyExists(company.getId());

        companyUserProvider.deleteAllByCompanyId(company.getId());
        companyCourseProvider.deleteAllByCompanyId(company.getId());
        companyProvider.delete(company);
    }

}
