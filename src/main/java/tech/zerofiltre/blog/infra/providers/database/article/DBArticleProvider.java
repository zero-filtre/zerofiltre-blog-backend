package tech.zerofiltre.blog.infra.providers.database.article;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.*;
import tech.zerofiltre.blog.infra.providers.database.article.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;

import java.time.LocalDate;
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
    public tech.zerofiltre.blog.domain.Page<Article> articlesOf(int pageNumber, int pageSize, Status status, long authorId, FinderRequest.Filter filter, String tag) {
        Page<ArticleJPA> page;

        final var publishedAtPropertyName = "publishedAt";
        if (authorId == 0) {
            if (tag != null)
                page = repository.findByStatusAndTagsName(PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, publishedAtPropertyName), status, tag);
            else if (FinderRequest.Filter.POPULAR == filter)
                page = repository.findByReactionsDesc(PageRequest.of(pageNumber, pageSize), status);
            else if (FinderRequest.Filter.MOST_VIEWED == filter)
                page = repository.findByViewsDesc(PageRequest.of(pageNumber, pageSize), status);
            else
                page = repository.findByStatus(PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, publishedAtPropertyName), status);
        } else {
            if (tag != null)
                page = repository.findByStatusAndAuthorIdAndTagsName(PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, publishedAtPropertyName), status, authorId, tag);
            else if (FinderRequest.Filter.POPULAR == filter)
                page = repository.findByReactionsAndAuthorIdDesc(PageRequest.of(pageNumber, pageSize), status, authorId);
            else if (FinderRequest.Filter.MOST_VIEWED == filter)
                page = repository.findByViewsAndAuthorIdDesc(PageRequest.of(pageNumber, pageSize), status, authorId);
            else
                page = repository.findByStatusAndAuthorId(PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, publishedAtPropertyName), status, authorId);
        }
        return pageMapper.fromSpringPage(page.map(mapper::fromJPA));
    }

    @Override
    public List<Article> articlesOf(User user) {
        return repository.findByAuthorId(user.getId())
                .stream().map(mapper::fromJPA).collect(Collectors.toList());
    }

    @Override
    public void delete(Article article) {
        ArticleJPA entity = mapper.toJPA(article);
        repository.delete(entity);
    }

    @Override
    public int countPublishedArticlesByDatesAndUser(LocalDate startDate, LocalDate endDate, long authorId) {
        return repository.countPublishedArticlesByDatesAndUser(startDate, endDate, authorId);
    }
}
