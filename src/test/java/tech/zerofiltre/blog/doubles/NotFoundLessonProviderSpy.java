package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.course.model.Lesson;

import java.util.List;
import java.util.Optional;

public class NotFoundLessonProviderSpy implements LessonProvider {
    @Override
    public Optional<Lesson> lessonOfId(long id) {
        return Optional.empty();
    }

    @Override
    public Lesson save(Lesson lesson) {
        return lesson;
    }

    @Override
    public void delete(Lesson lesson) {

    }

    @Override
    public List<Long> listNotCompletedLessons(long enrollmentId) { return List.of(); }

    @Override
    public List<Lesson> ofChapterId(long chapterId) {
        return List.of();
    }
}
