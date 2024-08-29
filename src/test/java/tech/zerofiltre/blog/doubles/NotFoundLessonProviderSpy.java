package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.LessonJPA;

import java.util.*;

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
}
