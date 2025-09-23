package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.model.Chapter;
import tech.zerofiltre.blog.domain.course.model.Lesson;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FoundChapterWithUnknownCourseProviderSpy implements ChapterProvider {

    public boolean chapterOfIdCalled;
    public boolean saveCalled;
    public boolean deleteCalled;

    @Override
    public Optional<Chapter> chapterOfId(long id) {
        chapterOfIdCalled = true;

        Chapter chapter = Chapter.builder()
                .id(1)
                .courseId(999)
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

    @Override
    public List<Chapter> ofCourseId(long courseId) {
        return Collections.emptyList();
    }
}
