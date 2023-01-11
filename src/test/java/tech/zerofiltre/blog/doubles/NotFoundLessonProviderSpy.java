package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class NotFoundLessonProviderSpy implements LessonProvider {
    @Override
    public Optional<Lesson> lessonOfId(long id) {
        return Optional.empty();
    }

    @Override
    public Lesson save(Lesson lesson) {
        return null;
    }

    @Override
    public void delete(Lesson lesson) {

    }
}
