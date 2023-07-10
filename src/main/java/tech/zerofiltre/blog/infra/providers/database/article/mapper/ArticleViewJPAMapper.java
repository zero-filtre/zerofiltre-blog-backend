package tech.zerofiltre.blog.infra.providers.database.article.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.*;

import java.util.*;

@Mapper(uses = {ArticleJPAMapper.class, UserJPAMapper.class})
public interface ArticleViewJPAMapper {

    ArticleView fromJPA(ArticleViewJPA articleViewJPA);

    List<ArticleView> fromJPAs(List<ArticleViewJPA> articleViewsJPA);

    ArticleViewJPA toJPA(ArticleView articleView);

    List<ArticleViewJPA> toJPAs(List<ArticleView> articleViews);
}
