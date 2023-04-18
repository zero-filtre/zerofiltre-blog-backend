package tech.zerofiltre.blog.infra.providers.database.article.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

@Mapper(uses = UserJPAMapper.class)
public interface ReactionArticleJPAMapper {

    @Mapping(target = "author", source = "authorId", qualifiedByName = "authorFromId")
    @Mapping(target = "article", source = "articleId", qualifiedByName = "articleFromId")
    public abstract ReactionArticleJPA toJPA(Reaction reaction);

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "articleId", source = "article.id")
    public abstract Reaction fromJPA(ReactionArticleJPA reactionArticleJPA);

    @Named("authorFromId")
    default UserJPA authorFromId(long authorId) {
        UserJPA userJPA = new UserJPA();
        userJPA.setId(authorId);
        return userJPA;

    }

    @Named("articleFromId")
    default ArticleJPA articleFromId(long articleId) {
        ArticleJPA articleJPA = new ArticleJPA();
        articleJPA.setId(articleId);
        return articleJPA;
    }
}
