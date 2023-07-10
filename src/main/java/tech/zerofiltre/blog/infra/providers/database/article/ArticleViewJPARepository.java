package tech.zerofiltre.blog.infra.providers.database.article;

import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;

import java.util.*;


public interface ArticleViewJPARepository extends JpaRepository<ArticleViewJPA, Long> {
    List<ArticleViewJPA> findByViewedId(long viewedId);

    List<ArticleViewJPA> findByViewerId(long viewerId);
}
