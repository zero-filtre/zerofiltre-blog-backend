package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public class FoundLessonProviderSpy implements LessonProvider {

    public boolean deleteCalled;

    @Override
    public Optional<Lesson> lessonOfId(long id) {
        return Optional.ofNullable(Lesson.builder().id(1).chapterId(1).build());
    }

    @Override
    public Lesson save(Lesson lesson) {
        return null;
    }

    @Override
    public void delete(Lesson lesson) {
        deleteCalled = true;
    }
}
