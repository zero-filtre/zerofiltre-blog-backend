package tech.zerofiltre.blog.infra.providers.database.course.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

@Mapper(uses = ResourceJPAMapper.class)
public interface LessonJPAMapper {

    @Mapping(target = "chapter", source = "chapterId", qualifiedByName = "chapterFromId")
    @Mapping(target = "number", source = "number", qualifiedByName = "fromNumber")
    LessonJPA toJPA(Lesson lesson);

    @Mapping(target = "chapterId", source = "chapter.id")
    @Mapping(target = "number", source = "number.number")
    Lesson fromJPA(LessonJPA lessonJPA);

    @Named("chapterFromId")
    default ChapterJPA chapterFromId(long chapterId) {
        ChapterJPA chapterJPA = new ChapterJPA();
        chapterJPA.setId(chapterId);
        return chapterJPA;
    }

    @Named("fromNumber")
    default LessonJPANumber fromNumber(int number) {
        LessonJPANumber lessonJPANumber = new LessonJPANumber();
        lessonJPANumber.setNumber(number);
        return lessonJPANumber;
    }
}
