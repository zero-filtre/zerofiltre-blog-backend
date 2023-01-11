package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class FoundChapterProviderSpy implements ChapterProvider {

    public boolean chapterOfIdCalled;
    public boolean saveCalled;
    public boolean deleteCalled;

    @Override
    public Optional<Chapter> chapterOfId(long id) {
        chapterOfIdCalled = true;
        return Optional.ofNullable(Chapter.builder().id(1).courseId(1).build());
    }

    @Override
    public Chapter save(Chapter chapter) {
        saveCalled = true;
        return chapter;
    }

    @Override
    public void delete(Chapter chapter) {
        deleteCalled = true;
    }
}
