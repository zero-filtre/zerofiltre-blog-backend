package tech.zerofiltre.blog.infra.providers.database.article;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.mapper.*;

import java.time.LocalDate;
import java.util.*;
@Component
@Transactional
@RequiredArgsConstructor
public class DBArticleViewProvider implements ArticleViewProvider {

    private final ArticleViewJPARepository repository;
    private final ArticleViewJPAMapper mapper = Mappers.getMapper(ArticleViewJPAMapper.class);


    @Override
    public List<ArticleView> viewsOfArticle(long viewedId) {
        return mapper.fromJPAs(repository.findByViewedId(viewedId));
    }

    @Override
    public ArticleView save(ArticleView articleView) {
        return mapper.fromJPA(repository.save(mapper.toJPA(articleView)));
    }

    @Override
    public List<ArticleView> viewsOfUser(long viewerId) {
        return mapper.fromJPAs(repository.findByViewerId(viewerId));
    }

    @Override
    public void delete(ArticleView articleView) {
        repository.deleteById(articleView.getId());
    }

    @Override
    public int countArticlesReadByDatesAndUser(LocalDate startDate, LocalDate endDate, long viewerId) {
        return repository.countViewedIdByDatesAndViewerId(startDate, endDate, viewerId);
    }
}
