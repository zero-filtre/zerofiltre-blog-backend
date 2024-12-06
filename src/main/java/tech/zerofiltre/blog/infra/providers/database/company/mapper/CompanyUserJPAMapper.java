package tech.zerofiltre.blog.infra.providers.database.company.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.infra.providers.database.company.model.CompanyUserJPA;

@Mapper
public interface CompanyUserJPAMapper {

    CompanyUserJPA toJPA(LinkCompanyUser linkCompanyUser);

    LinkCompanyUser fromJPA(CompanyUserJPA companyUserJPA);

    /*
    @Mapping(target = "company", source = "companyId", qualifiedByName = "companyFromId")
    @Mapping(target = "user", source = "userId", qualifiedByName = "userFromId")
    @Mapping(target = "id.companyId", source = "companyId")
    @Mapping(target = "id.userId", source = "userId")
    CompanyUserJPA toJPA(CompanyUser companyUser);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "userId", source = "user.id")
    CompanyUser fromJPA(CompanyUserJPA companyUserJPA);

    @Named("companyFromId")
    default CompanyJPA companyFromId(long companyId) {
        CompanyJPA companyJPA = new CompanyJPA();
        companyJPA.setId(companyId);
        return companyJPA;
    }

    @Named("userFromId")
    default UserJPA userFromId(long userId) {
        UserJPA userJPA = new UserJPA();
        userJPA.setId(userId);
        return userJPA;
    }
*/
}
