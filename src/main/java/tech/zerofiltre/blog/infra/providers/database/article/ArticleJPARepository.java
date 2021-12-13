package tech.zerofiltre.blog.infra.providers.database.article;

import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;

public interface ArticleJPARepository extends JpaRepository<ArticleJPA, Long> {
}
