package tech.zerofiltre.blog.infra.providers.database.article.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;

@Mapper
public interface TagJPAMapper {

    TagJPA toJPA(Tag tag);

    Tag fromJPA(TagJPA tagJPA);
}
