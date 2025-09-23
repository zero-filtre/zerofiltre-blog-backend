package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.course.model.Lesson;

import java.util.List;
import java.util.Optional;

public class FoundLessonProviderSpy implements LessonProvider {

    public boolean deleteCalled;
    public long calledLessonId;

    @Override
    public Optional<Lesson> lessonOfId(long lessonId) {
        calledLessonId = lessonId;
        return Optional.ofNullable(Lesson.builder()
                .id(1)
                .title("Lesson 1")
                .chapterId(1).build());
    }

    @Override
    public Lesson save(Lesson lesson) {
        return lesson;
    }

    @Override
    public void delete(Lesson lesson) {
        deleteCalled = true;
    }

    @Override
    public List<Long> listNotCompletedLessons(long enrollmentId) { return List.of(); }
}
