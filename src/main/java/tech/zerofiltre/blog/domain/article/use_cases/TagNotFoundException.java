package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.*;

public class TagNotFoundException extends BlogException {

    private final long tagId;

    public TagNotFoundException(String message, long tagId) {
        super(message);
        this.tagId = tagId;
    }

    public long getTagId() {
        return tagId;
    }
}
