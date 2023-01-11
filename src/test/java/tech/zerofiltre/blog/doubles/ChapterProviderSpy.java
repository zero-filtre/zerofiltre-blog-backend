package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class ChapterProviderSpy implements ChapterProvider {

    public boolean chapterOfIdCalled;
    public boolean saveCalled;
    public boolean deleteCalled;

    @Override
    public Optional<Chapter> chapterOfId(long id) {
        chapterOfIdCalled = true;
        return Optional.empty();
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
