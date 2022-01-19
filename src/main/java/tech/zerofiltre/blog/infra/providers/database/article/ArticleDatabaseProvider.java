package tech.zerofiltre.blog.infra.providers.database.article;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;

import java.util.*;

@Component
@Transactional
@RequiredArgsConstructor
public class ArticleDatabaseProvider implements ArticleProvider {

    private final ArticleJPARepository repository;
    private final ArticleJPAMapper mapper = Mappers.getMapper(ArticleJPAMapper.class);


    @Override
    public Optional<Article> articleOfId(long articleId) {
        return repository.findById(articleId)
                .map(mapper::fromJPA);
    }


    @Override
    public Article save(Article article) {
        ArticleJPA save = repository.save(mapper.toJPA(article));
        return mapper.fromJPA(save);
    }

    @Override
    public List<Article> articlesOf(int pageNumber, int pageSize) {
        return repository.findAll(PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC,"id"))
                .map(mapper::fromJPA)
                .getContent();
    }


}
