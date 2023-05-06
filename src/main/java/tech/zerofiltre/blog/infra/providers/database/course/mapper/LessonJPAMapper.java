package tech.zerofiltre.blog.infra.providers.database.course.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

import java.util.*;

@Mapper(uses = ResourceJPAMapper.class)
public interface LessonJPAMapper {

    @Mapping(target = "chapter", source = "chapterId", qualifiedByName = "chapterFromId")
    LessonJPA toJPA(Lesson lesson);

    @Mapping(target = "chapterId", source = "chapter.id")
    Lesson fromJPA(LessonJPA lessonJPA);

    @Named("chapterFromId")
    default ChapterJPA chapterFromId(long chapterId) {
        ChapterJPA chapterJPA = new ChapterJPA();
        chapterJPA.setId(chapterId);
        return chapterJPA;
    }

    List<LessonJPA> toJPAs(List<Lesson> lessons);

    List<Lesson> fromJPAs(List<LessonJPA> lessonsJPA);
}
