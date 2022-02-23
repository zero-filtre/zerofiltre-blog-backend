package tech.zerofiltre.blog.infra.providers.database.user.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

@Mapper
public abstract class SocialLinkJPAMapper {

    @Mapping(target = "userId", source = "user.id")
    abstract SocialLink fromJPA(SocialLinkJPA socialLinkJPA);

    @Mapping(target = "user", source = "userId", qualifiedByName = "userFromId")
    abstract SocialLinkJPA toJPA(SocialLink socialLink);

    @Named("userFromId")
    UserJPA userFromId(long userId) {
        UserJPA userJPA = new UserJPA();
        userJPA.setId(userId);
        return userJPA;
    }
}
