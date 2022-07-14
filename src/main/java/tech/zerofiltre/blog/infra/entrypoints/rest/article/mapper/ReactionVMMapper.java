package tech.zerofiltre.blog.infra.entrypoints.rest.article.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.model.*;

import java.util.*;

@Mapper
public interface ReactionVMMapper {

    ReactionVM toVM(Reaction reaction);

    List<ReactionVM> toVMs(List<Reaction> reactions);
}
