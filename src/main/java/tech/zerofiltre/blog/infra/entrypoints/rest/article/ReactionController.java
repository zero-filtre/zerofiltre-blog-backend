package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import org.mapstruct.factory.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.*;
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
    public List<ReactionVM> addReaction(@RequestParam(required = false, defaultValue = "0") long articleId, @RequestParam(required = false, defaultValue = "0") long courseId, @RequestParam String action) throws ResourceNotFoundException, ForbiddenActionException {
        if (articleId == 0 && courseId == 0) {
            throw new ResourceNotFoundException("You must provide either an articleId or a courseId", String.valueOf(0), Domains.REACTION.name());
        }
        Reaction reaction = new Reaction();
        if (articleId != 0) reaction.setArticleId(articleId);
        if (courseId != 0) reaction.setCourseId(courseId);
        reaction.setAuthorId(securityContextManager.getAuthenticatedUser().getId());
        action = action.toUpperCase(Locale.ROOT);
        reaction.setAction(Reaction.Action.valueOf(action));
        return mapper.toVMs(addReaction.execute(reaction));

    }
}
