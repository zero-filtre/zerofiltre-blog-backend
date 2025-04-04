package tech.zerofiltre.blog.infra.providers.database.article;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.zerofiltre.blog.domain.article.ArticleViewProvider;
import tech.zerofiltre.blog.domain.article.model.ArticleView;
import tech.zerofiltre.blog.infra.providers.database.article.mapper.ArticleViewJPAMapper;

import java.time.LocalDateTime;
import java.util.List;
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
    public int countArticlesReadByDatesAndUser(LocalDateTime startDate, LocalDateTime endDate, long viewerId) {
        return repository.countViewedIdByDatesAndViewerId(startDate, endDate, viewerId);
    }
}
