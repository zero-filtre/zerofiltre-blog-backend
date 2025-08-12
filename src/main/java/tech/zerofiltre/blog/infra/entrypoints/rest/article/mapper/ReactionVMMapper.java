package tech.zerofiltre.blog.infra.entrypoints.rest.article.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.article.model.Reaction;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.model.ReactionVM;

import java.util.List;

@Mapper
public interface ReactionVMMapper {

    ReactionVM toVM(Reaction reaction);

    List<ReactionVM> toVMs(List<Reaction> reactions);
}
