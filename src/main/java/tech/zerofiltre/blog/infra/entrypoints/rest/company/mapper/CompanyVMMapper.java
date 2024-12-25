package tech.zerofiltre.blog.infra.entrypoints.rest.company.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.model.UpdateCompanyVM;

@Mapper
public interface CompanyVMMapper {

    Company fromVM(UpdateCompanyVM updateCompanyVM);

    UpdateCompanyVM toVM(Company company);
}
