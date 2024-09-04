package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import org.mapstruct.factory.Mappers;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.model.Reaction;
import tech.zerofiltre.blog.domain.article.use_cases.AddReaction;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.mapper.ReactionVMMapper;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.model.ReactionVM;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/reaction")
public class ReactionController {

    private final AddReaction addReaction;
    private final SecurityContextManager securityContextManager;
    private final ReactionVMMapper mapper = Mappers.getMapper(ReactionVMMapper.class);


    public ReactionController(ArticleProvider articleProvider, CourseProvider courseProvider, SecurityContextManager securityContextManager) {
        addReaction = new AddReaction(articleProvider, courseProvider);
        this.securityContextManager = securityContextManager;
    }


    @PostMapping
    public List<ReactionVM> addReaction(@RequestParam(required = false, defaultValue = "0") long articleId, @RequestParam(required = false, defaultValue = "0") long courseId, @RequestParam String action) throws ResourceNotFoundException, ForbiddenActionException {
        if (articleId == 0 && courseId == 0) {
            throw new ResourceNotFoundException("You must provide either an articleId or a courseId", String.valueOf(0));
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
