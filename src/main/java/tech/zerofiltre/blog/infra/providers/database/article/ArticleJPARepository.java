package tech.zerofiltre.blog.infra.providers.database.article;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;

import java.util.*;

public interface ArticleJPARepository extends JpaRepository<ArticleJPA, Long> {
    Page<ArticleJPA> findByStatus(Pageable pageable, Status status);

    Page<ArticleJPA> findByStatusAndAuthorId(Pageable pageable, Status status, long authorId);

    List<ArticleJPA> findByAuthorId(long userId);

    @Query("select a from ArticleJPA a WHERE a.status=?1 ORDER BY a.reactions.size desc ")
    Page<ArticleJPA> findByReactionsDesc(Pageable pageable, Status status);
}
