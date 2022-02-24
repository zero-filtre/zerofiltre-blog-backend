package tech.zerofiltre.blog.infra.providers.database.article;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.*;
import tech.zerofiltre.blog.infra.providers.database.article.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;

import java.util.*;
import java.util.stream.*;

@Component
@Transactional
@RequiredArgsConstructor
public class DBArticleProvider implements ArticleProvider {

    private final ArticleJPARepository repository;
    private final ArticleJPAMapper mapper = Mappers.getMapper(ArticleJPAMapper.class);
    private final SpringPageMapper<Article> pageMapper = new SpringPageMapper<>();


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
    public tech.zerofiltre.blog.domain.Page<Article> articlesOf(int pageNumber, int pageSize, Status status, long authorId, boolean byPopularity, String tag) {
        Page<ArticleJPA> page;
        if (authorId == 0 && byPopularity)
            page = repository.findByReactionsDesc(PageRequest.of(pageNumber, pageSize),status);
        else if (authorId == 0)
            page = repository.findByStatus(PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, "id"), status);
        else
            page = repository.findByStatusAndAuthorId(PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, "id"), status, authorId);

        return pageMapper.fromSpringPage(page.map(mapper::fromJPA));
    }

    @Override
    public List<Article> articlesOf(User user) {
        return repository.findByAuthorId(user.getId())
                .stream().map(mapper::fromJPA).collect(Collectors.toList());
    }


}
