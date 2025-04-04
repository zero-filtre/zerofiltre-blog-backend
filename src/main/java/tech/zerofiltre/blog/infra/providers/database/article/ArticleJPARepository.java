package tech.zerofiltre.blog.infra.providers.database.article;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.infra.providers.database.article.model.ArticleJPA;

import java.time.LocalDateTime;
import java.util.List;

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

    @Query("SELECT a FROM ArticleJPA a WHERE a.status = :status AND " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.summary) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ArticleJPA> findByKeyword(String keyword, Status status);

    @Query("SELECT a FROM ArticleJPA a WHERE a.lastPublishedAt >= ?1 AND a.lastPublishedAt < ?2")
    List<ArticleJPA> findNewArticlesBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT count(a.id) FROM ArticleJPA a WHERE a.lastPublishedAt >= ?1 AND a.lastPublishedAt < ?2 AND a.author.id = ?3")
    int countPublishedArticlesByDatesAndUser(LocalDateTime startDate, LocalDateTime endDate, long authorId);

}
