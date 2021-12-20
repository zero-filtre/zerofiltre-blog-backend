package tech.zerofiltre.blog.domain.article;

import tech.zerofiltre.blog.domain.article.model.*;

import java.util.*;

public interface TagProvider {

    Optional<Tag> tagOfId(long tag);

    List<Tag> tags();

    Tag create(Tag tag);
}
