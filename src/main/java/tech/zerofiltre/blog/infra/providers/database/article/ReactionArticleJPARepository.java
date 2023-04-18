package tech.zerofiltre.blog.infra.providers.database.article;

import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

import java.util.*;

public interface ReactionArticleJPARepository extends JpaRepository<ReactionArticleJPA, Long> {
    List<ReactionArticleJPA> findByAuthor(UserJPA author);
}
