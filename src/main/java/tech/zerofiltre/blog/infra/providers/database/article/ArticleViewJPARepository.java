package tech.zerofiltre.blog.infra.providers.database.article;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.*;

public interface ArticleViewJPARepository extends JpaRepository<ArticleViewJPA, Long> {
    List<ArticleViewJPA> findByViewedId(long viewedId);

    List<ArticleViewJPA> findByViewerId(long viewerId);

    @Query(value = "select count(distinct a.viewed.id) from articleViewJPA a WHERE a.viewed.at>=?1 AND a.viewed.at<?2 AND a.viewer.id=:?3")
    int countViewedIdByDatesAndViewerId(LocalDate startDate, LocalDate endDate, long viewerId);

}
