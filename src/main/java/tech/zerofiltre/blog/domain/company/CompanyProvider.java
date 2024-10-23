package tech.zerofiltre.blog.domain.company;

import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.company.model.Company;

import java.util.Optional;

public interface CompanyProvider {
    Company save(Company company);

    Optional<Company> findById(long id);

    Page<Company> findAll(int pageNumber, int pageSize);

    void delete(Company company);

}
