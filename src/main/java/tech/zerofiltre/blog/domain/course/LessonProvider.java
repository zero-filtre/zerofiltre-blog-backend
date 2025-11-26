package tech.zerofiltre.blog.domain.course;

import tech.zerofiltre.blog.domain.course.model.Lesson;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface LessonProvider {
    Optional<Lesson> lessonOfId(long id);
    Lesson save(Lesson lesson);
    void delete(Lesson lesson);
    List<Long> listNotCompletedLessons(long enrollmentId);
    List<Lesson> ofChapterId(long chapterId);

    default List<Lesson> saveAll(List<Lesson> lessons){
        return new ArrayList<>();
    }
}
