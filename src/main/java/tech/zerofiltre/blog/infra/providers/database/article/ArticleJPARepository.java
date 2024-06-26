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

    @Query("select a from ArticleJPA a WHERE a.status=?1 ORDER BY size(a.reactions) desc ")
    Page<ArticleJPA> findByReactionsDesc(Pageable pageable, Status status);

    @Query("select a from ArticleJPA a WHERE a.status=?1 ORDER BY viewsCount desc ")
    Page<ArticleJPA> findByViewsDesc(Pageable pageable, Status status);

    Page<ArticleJPA> findByStatusAndTagsName(Pageable pageable, Status status, String tag);

    Page<ArticleJPA> findByStatusAndAuthorIdAndTagsName(Pageable pageable, Status status, long authorId, String tagName);

    @Query("select a from ArticleJPA a WHERE a.status=?1 AND a.author.id=?2 ORDER BY size(a.reactions) desc ")
    Page<ArticleJPA> findByReactionsAndAuthorIdDesc(Pageable pageable, Status status, long authorId);

    @Query("select a from ArticleJPA a WHERE a.status=?1 AND a.author.id=?2 ORDER BY viewsCount desc ")
    Page<ArticleJPA> findByViewsAndAuthorIdDesc(Pageable pageable, Status status, long authorId);

    @Query(value = "select count(distinct a.author_id) from article a WHERE a.last_published_at>=?1 AND a.last_published_at<?2 AND a.author_id=?3", nativeQuery = true)
    int countByDateStartAndDateEndAndAuthorId(String dateStart, String dateEnd, long authorId);
}
