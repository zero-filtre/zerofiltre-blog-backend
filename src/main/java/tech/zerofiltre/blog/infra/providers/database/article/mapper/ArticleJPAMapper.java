package tech.zerofiltre.blog.infra.providers.database.article.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.*;

@Mapper(uses = {TagJPAMapper.class, UserJPAMapper.class, ReactionJPAMapper.class})
public interface ArticleJPAMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Article fromJPA(ArticleJPA articleJPA);

    ArticleJPA toJPA(Article article);
}
