package tech.zerofiltre.blog.infra.entrypoints.rest.company.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.infra.entrypoints.rest.company.model.UserCompanyInfoVM;

import java.util.List;

@Mapper
public interface UserCompanyInfoVMMapper {

    List<UserCompanyInfoVM> toVM(List<LinkCompanyUser> companyUserList);

}
