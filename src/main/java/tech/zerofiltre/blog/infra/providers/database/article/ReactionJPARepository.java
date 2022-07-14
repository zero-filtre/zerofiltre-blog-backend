package tech.zerofiltre.blog.infra.providers.database.article;

import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

import java.util.*;

public interface ReactionJPARepository extends JpaRepository<ReactionJPA, Long> {
    List<ReactionJPA> findByAuthor(UserJPA author);
}
