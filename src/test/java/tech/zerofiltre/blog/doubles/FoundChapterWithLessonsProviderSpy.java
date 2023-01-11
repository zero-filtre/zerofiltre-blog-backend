package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class FoundChapterWithLessonsProviderSpy implements ChapterProvider {

    public boolean chapterOfIdCalled;
    public boolean saveCalled;
    public boolean deleteCalled;

    @Override
    public Optional<Chapter> chapterOfId(long id) {
        chapterOfIdCalled = true;
        Chapter chapter = Chapter.builder()
                .id(1)
                .courseId(1)
                .title("Chapter 1")
                .build();
        chapter.getLessons().add(Lesson.builder().id(1).chapterId(1).build());
        return Optional.of(chapter);
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
