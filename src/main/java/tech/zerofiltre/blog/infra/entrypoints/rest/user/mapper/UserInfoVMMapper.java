package tech.zerofiltre.blog.infra.entrypoints.rest.user.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.UserInfoVM;

@Mapper
public interface UserInfoVMMapper {

    UserInfoVM toVM(User user);

}
