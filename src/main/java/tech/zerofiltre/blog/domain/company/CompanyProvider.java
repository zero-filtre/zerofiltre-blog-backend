package tech.zerofiltre.blog.domain.company;

import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.model.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyProvider {
    Company save(Company company);

    Optional<Company> findById(long id);

    Page<Company> findAll(int pageNumber, int pageSize);

    Page<Company> findAllByUserId(int pageNumber, int pageSize, long userId);

    List<Long> findAllCompanyIdByUserIdAndCourseId(long userId, long courseId);

    void delete(Company company);

}
