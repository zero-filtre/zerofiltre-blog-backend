package tech.zerofiltre.blog.domain.course;

import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public interface LessonProvider {
    Optional<Lesson> lessonOfId(long id);
    Lesson save(Lesson lesson);
    void delete(Lesson lesson);
}
