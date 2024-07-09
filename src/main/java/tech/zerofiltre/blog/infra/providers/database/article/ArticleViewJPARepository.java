package tech.zerofiltre.blog.infra.providers.database.article;

import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;

import java.time.LocalDate;
import java.util.*;

public interface ArticleViewJPARepository extends JpaRepository<ArticleViewJPA, Long> {
    List<ArticleViewJPA> findByViewedId(long viewedId);

    List<ArticleViewJPA> findByViewerId(long viewerId);

    @Query(value = "select count(distinct a.viewed_id) from article_view a WHERE a.viewed_at>=?1 AND a.viewed_at<?2 AND a.viewer_id=?3", nativeQuery = true)
    int countViewedIdByDatesAndViewerId(LocalDate startDate, LocalDate endDate, long viewerId);

}
