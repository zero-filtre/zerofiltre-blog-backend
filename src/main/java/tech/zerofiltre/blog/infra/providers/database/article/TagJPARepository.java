package tech.zerofiltre.blog.infra.providers.database.article;

import org.springframework.data.jpa.repository.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;

import java.util.*;

public interface TagJPARepository extends JpaRepository<TagJPA, Long> {
    Optional<TagJPA> findByName(String name);
}
