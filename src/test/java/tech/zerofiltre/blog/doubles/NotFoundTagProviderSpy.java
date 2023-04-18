package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;

import java.util.*;

public class NotFoundTagProviderSpy implements TagProvider {

    public boolean tagOfIdCalled;

    @Override
    public Optional<Tag> tagOfId(long id) {
        tagOfIdCalled = true;
        return Optional.empty();
    }

    @Override
    public Optional<Tag> tagOfName(String name) {
        return Optional.empty();
    }

    @Override
    public List<Tag> tags() {
        return null;
    }

    @Override
    public Tag save(Tag tag) {
        return null;
    }
}
