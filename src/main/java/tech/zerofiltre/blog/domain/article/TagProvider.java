package tech.zerofiltre.blog.domain.article;

import tech.zerofiltre.blog.domain.article.model.*;

import java.util.*;

public interface TagProvider {

    Optional<Tag> tagOfId(long id);

    Optional<Tag> tagOfName(String name);

    List<Tag> tags();

    Tag save(Tag tag);
}
