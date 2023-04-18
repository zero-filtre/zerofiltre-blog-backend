package tech.zerofiltre.blog.infra.providers.database.article;

import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

import java.util.*;

public interface ReactionCourseJPARepository extends JpaRepository<ReactionCourseJPA, Long> {
    List<ReactionCourseJPA> findByAuthor(UserJPA author);
}
