package tech.zerofiltre.blog.infra.providers.database.company.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.company.model.Company;
import tech.zerofiltre.blog.infra.providers.database.company.model.CompanyJPA;

@Mapper
public interface CompanyJPAMapper {

    Company fromJPA(CompanyJPA companyJPA);

    CompanyJPA toJPA(Company company);
}
