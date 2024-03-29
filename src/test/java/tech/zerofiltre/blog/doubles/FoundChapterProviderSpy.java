package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class FoundChapterProviderSpy implements ChapterProvider {

    public boolean chapterOfIdCalled;
    public boolean saveCalled;
    public boolean ofCourseIdCalled;
    public boolean deleteCalled;
    public long calledChapterId;


    @Override
    public Optional<Chapter> chapterOfId(long chapterId) {
        chapterOfIdCalled = true;
        calledChapterId = chapterId;
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

    @Override
    public List<Chapter> ofCourseId(long courseId) {
        ofCourseIdCalled = true;
        List<Chapter> chapters = new ArrayList<>();
        chapters.add(Chapter.builder().id(1).courseId(courseId).lessons(Collections.singletonList(Lesson.builder().build())).build());
        chapters.add(Chapter.builder().id(2).courseId(courseId).lessons(Collections.singletonList(Lesson.builder().build())).build());
        return chapters;
    }
}
