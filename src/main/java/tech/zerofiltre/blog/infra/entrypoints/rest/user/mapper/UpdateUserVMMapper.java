package tech.zerofiltre.blog.infra.entrypoints.rest.user.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.*;

@Mapper
public interface UpdateUserVMMapper {

    User fromVM(UpdateUserVM updateUserVM);

    UpdateUserVM toVM(User user);
}
