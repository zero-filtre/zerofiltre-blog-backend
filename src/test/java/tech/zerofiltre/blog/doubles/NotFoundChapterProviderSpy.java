package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class NotFoundChapterProviderSpy implements ChapterProvider {

    @Override
    public Optional<Chapter> chapterOfId(long id) {
        return Optional.empty();
    }

    @Override
    public Chapter save(Chapter chapter) {
        return null;
    }

    @Override
    public void delete(Chapter chapter) {

    }
}
