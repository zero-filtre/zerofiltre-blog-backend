package tech.zerofiltre.blog.infra.providers.database.article.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.infra.providers.database.article.model.TagJPA;

import java.util.List;
import java.util.Set;

@Mapper
public interface TagJPAMapper {

    TagJPA toJPA(Tag tag);

    Tag fromJPA(TagJPA tagJPA);

    List<Tag> fromJPA(Set<TagJPA> tagJPA);
}
