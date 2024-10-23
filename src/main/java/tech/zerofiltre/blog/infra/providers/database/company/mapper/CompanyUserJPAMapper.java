package tech.zerofiltre.blog.infra.providers.database.company.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.infra.providers.database.company.model.LinkCompanyUserJPA;

@Mapper
public interface CompanyUserJPAMapper {

    LinkCompanyUserJPA toJPA(LinkCompanyUser linkCompanyUser);

    LinkCompanyUser fromJPA(LinkCompanyUserJPA linkCompanyUserJPA);

}
