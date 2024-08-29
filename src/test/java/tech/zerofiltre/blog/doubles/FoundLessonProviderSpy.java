package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.LessonJPA;

import java.util.*;

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
