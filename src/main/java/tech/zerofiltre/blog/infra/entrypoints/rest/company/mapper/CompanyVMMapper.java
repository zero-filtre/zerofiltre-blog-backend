package tech.zerofiltre.blog.infra.entrypoints.rest.company.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.model.CompanyVM;

@Mapper
public interface CompanyVMMapper {

    Company fromVM(CompanyVM companyVM);

    CompanyVM toVM(Company company);
}
