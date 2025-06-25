package tech.zerofiltre.blog.domain.course;

import org.springframework.data.util.Pair;
import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public interface LessonProvider {
    Optional<Lesson> lessonOfId(long id);
    Lesson save(Lesson lesson);
    void delete(Lesson lesson);
    List<Long> listNotCompletedLessons(long enrollmentId);
    List<String> getCourseChapterLessonTitle(String lessonId);

    default List<Lesson> saveAll(List<Lesson> lessons){
        return new ArrayList<>();
    }
}
