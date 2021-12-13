package tech.zerofiltre.blog.infra.providers.database.user.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

@Mapper
public interface UserJPAMapper {

    UserJPA toJPA(User user);

    User fromJPA(UserJPA userJPA);

}
