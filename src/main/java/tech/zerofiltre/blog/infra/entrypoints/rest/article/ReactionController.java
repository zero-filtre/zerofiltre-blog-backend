package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import org.mapstruct.factory.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.article.use_cases.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.mapper.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.model.*;

import java.util.*;

@RestController
@RequestMapping("/reaction")
public class ReactionController {

    private final AddReaction addReaction;
    private final SecurityContextManager securityContextManager;
    private final ReactionVMMapper mapper = Mappers.getMapper(ReactionVMMapper.class);


    public ReactionController(ArticleProvider articleProvider, SecurityContextManager securityContextManager) {
        addReaction = new AddReaction(articleProvider);
        this.securityContextManager = securityContextManager;
    }


    @PostMapping
    public List<ReactionVM> addReaction(@RequestParam long articleId, @RequestParam String action) throws ResourceNotFoundException, ForbiddenActionException {
        Reaction reaction = new Reaction();
        reaction.setArticleId(articleId);
        reaction.setAuthorId(securityContextManager.getAuthenticatedUser().getId());
        action = action.toUpperCase(Locale.ROOT);
        reaction.setAction(Reaction.Action.valueOf(action));
        return mapper.toVMs(addReaction.execute(reaction));

    }
}
