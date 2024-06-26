package tech.zerofiltre.blog.infra.providers.database.article;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.article.ArticleViewByDatesProvider;

@Component
@RequiredArgsConstructor
public class DBArticleViewByDatesProvider implements ArticleViewByDatesProvider {

    private final ArticleViewJPARepository repository;

    @Override
    public int countByUser(String dateStart, String dateEnd, long viewerId) {
        return repository.countByDateStartAndDateEndAndViewerId(dateStart, dateEnd, viewerId);
    }

}
