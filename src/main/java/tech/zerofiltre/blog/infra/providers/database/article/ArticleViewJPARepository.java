package tech.zerofiltre.blog.infra.providers.database.article;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.zerofiltre.blog.infra.providers.database.article.model.ArticleViewJPA;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleViewJPARepository extends JpaRepository<ArticleViewJPA, Long> {
    List<ArticleViewJPA> findByViewedId(long viewedId);

    List<ArticleViewJPA> findByViewerId(long viewerId);

    @Query("SELECT count(distinct a.viewed.id) FROM ArticleViewJPA a WHERE a.viewedAt >= ?1 AND a.viewedAt < ?2 AND a.viewer.id = ?3")
    int countViewedIdByDatesAndViewerId(LocalDateTime startDate, LocalDateTime endDate, long viewerId);

}
