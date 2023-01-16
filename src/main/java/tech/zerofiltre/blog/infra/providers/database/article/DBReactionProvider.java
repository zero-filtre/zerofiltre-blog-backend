package tech.zerofiltre.blog.infra.providers.database.article;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.*;

import java.util.*;
import java.util.stream.*;

@Component
@Transactional
@RequiredArgsConstructor
public class DBReactionProvider implements ReactionProvider {

    private final ReactionArticleJPARepository reactionArticleRepository;
    private final ReactionCourseJPARepository reactionCourseRepository;
    private final ReactionArticleJPAMapper reactionArticleJPAMapper = Mappers.getMapper(ReactionArticleJPAMapper.class);
    private final ReactionCourseJPAMapper reactionCourseJPAMapper = Mappers.getMapper(ReactionCourseJPAMapper.class);
    private final UserJPAMapper userMapper = Mappers.getMapper(UserJPAMapper.class);


    @Override
    public Optional<Reaction> reactionOfId(long reactionId) {
        Optional<ReactionArticleJPA> reactionArticleJPA = reactionArticleRepository.findById(reactionId);
        Optional<ReactionCourseJPA> reactionCourseJPA = reactionCourseRepository.findById(reactionId);
        if (reactionArticleJPA.isPresent())
            return reactionArticleJPA.map(reactionArticleJPAMapper::fromJPA);
        if (reactionCourseJPA.isPresent())
            return reactionCourseJPA.map(reactionCourseJPAMapper::fromJPA);
        return Optional.empty();
    }

    @Override
    public List<Reaction> reactions() {
        List<Reaction> result = reactionArticleRepository.findAll()
                .stream().map(reactionArticleJPAMapper::fromJPA)
                .collect(Collectors.toList());

        result.addAll(
                reactionCourseRepository.findAll()
                        .stream().map(reactionCourseJPAMapper::fromJPA)
                        .collect(Collectors.toList())
        );
        return result;
    }

    @Override
    public List<Reaction> ofUser(User user) {

        List<ReactionArticleJPA> articleReactions = reactionArticleRepository.findByAuthor(userMapper.toJPA(user));
        List<ReactionCourseJPA> courseReactions = reactionCourseRepository.findByAuthor(userMapper.toJPA(user));

        List<Reaction> result = articleReactions.stream().map(reactionArticleJPAMapper::fromJPA).collect(Collectors.toList());
        result.addAll(courseReactions.stream().map(reactionCourseJPAMapper::fromJPA).collect(Collectors.toList()));
        return result;
    }


    @Override
    public Reaction save(Reaction reaction) {
        if (reaction.getArticleId() != 0)
            return reactionArticleJPAMapper.fromJPA(reactionArticleRepository.save(reactionArticleJPAMapper.toJPA(reaction)));
        else
            return reactionCourseJPAMapper.fromJPA(reactionCourseRepository.save(reactionCourseJPAMapper.toJPA(reaction)));
    }

    @Override
    public void delete(Reaction reaction) {
        if (reaction.getArticleId() != 0)
            reactionArticleRepository.delete(reactionArticleJPAMapper.toJPA(reaction));
        else
            reactionCourseRepository.delete(reactionCourseJPAMapper.toJPA(reaction));
    }
}
