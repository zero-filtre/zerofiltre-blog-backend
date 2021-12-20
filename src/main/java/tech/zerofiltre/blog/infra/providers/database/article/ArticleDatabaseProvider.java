package tech.zerofiltre.blog.infra.providers.database.article;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.mapper.*;

import java.util.*;
import java.util.stream.*;

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
    public List<Article> articles() {
        return repository.findAll()
                .stream().map(mapper::fromJPA)
                .collect(Collectors.toList());
    }

    @Override
    public Article save(Article article) {
        return mapper.fromJPA(repository.save(mapper.toJPA(article)));
    }


}
