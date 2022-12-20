package tech.zerofiltre.blog.domain.course;

import tech.zerofiltre.blog.domain.course.model.*;

import java.util.*;

public interface ChapterProvider {
    Optional<Chapter> chapterOfId(long id);
    Chapter registerChapter(Chapter chapter);
}
