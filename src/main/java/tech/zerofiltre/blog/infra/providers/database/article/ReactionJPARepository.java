package tech.zerofiltre.blog.infra.providers.database.article;

import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;

public interface ReactionJPARepository extends JpaRepository<ReactionJPA, Long> {
}
