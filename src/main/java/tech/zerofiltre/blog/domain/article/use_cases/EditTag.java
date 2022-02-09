package tech.zerofiltre.blog.domain.article.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.error.*;

public class EditTag {

    private final TagProvider tagProvider;

    public EditTag(TagProvider tagProvider) {
        this.tagProvider = tagProvider;
    }

    public Tag create(Tag tag) throws ResourceAlreadyExistException {
        if (tagProvider.tagOfName(tag.getName()).isPresent())
            throw new ResourceAlreadyExistException("A tag with this name already exist.", tag.getName(), Domains.TAG.name());
        return tagProvider.save(tag);
    }

    public Tag update(Tag tag) throws ResourceNotFoundException {
        if (tagProvider.tagOfId(tag.getId()).isEmpty()) {
            throw new ResourceNotFoundException("We couldn't find a tag of id: " + tag.getId() + " to update", tag.getId(), Domains.TAG.name());
        }
        return tagProvider.save(tag);
    }
}
