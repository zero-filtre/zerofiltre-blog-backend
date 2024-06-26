package tech.zerofiltre.blog.infra.providers.database.article;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.article.ArticleByDatesProvider;

@Component
@RequiredArgsConstructor
public class DBArticleByDatesProvider implements ArticleByDatesProvider {

    private final ArticleJPARepository repository;

    @Override
    public int countByUser(String dateStart, String dateEnd, long authorId) {
        return repository.countByDateStartAndDateEndAndAuthorId(dateStart, dateEnd, authorId);
    }

}
