package tech.zerofiltre.blog.infra.entrypoints.rest.company.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.model.RegisterCompanyVM;

@Mapper
public interface RegisterCompanyVMMapper {

    Company fromVM(RegisterCompanyVM registerCompanyVM);

    RegisterCompanyVM toVM(Company company);
}
