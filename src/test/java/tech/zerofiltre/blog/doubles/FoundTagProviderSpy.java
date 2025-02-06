package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

public class FoundTagProviderSpy implements TagProvider {
    @Override
    public Optional<Tag> tagOfId(long id) {
        return Optional.of(ZerofiltreUtilsTest.createMockTags(true).get(0));
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
