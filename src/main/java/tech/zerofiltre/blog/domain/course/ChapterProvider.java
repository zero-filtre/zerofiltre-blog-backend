package tech.zerofiltre.blog.domain.course;

import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public interface ChapterProvider {
    Optional<Chapter> chapterOfId(long id);

    Chapter save(Chapter chapter);

    void delete(Chapter chapter);

    List<Chapter> ofCourseId(long courseId);

    default List<Chapter> saveAll(List<Chapter> chapters) {
        return new ArrayList<>();
    }
}
