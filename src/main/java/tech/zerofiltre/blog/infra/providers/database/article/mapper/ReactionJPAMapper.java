package tech.zerofiltre.blog.infra.providers.database.article.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;

@Mapper
public interface ReactionJPAMapper {

    ReactionJPA toJPA(Reaction reaction);

    Reaction fromJPA(ReactionJPA reactionJPA);
}
