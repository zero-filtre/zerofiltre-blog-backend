package tech.zerofiltre.blog.infra.entrypoints.rest.user.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.*;

@Mapper
public interface PublicUserProfileVMMapper {

    User fromVM(PublicUserProfileVM publicUserProfileVM);

    PublicUserProfileVM toVM(User user);
}
