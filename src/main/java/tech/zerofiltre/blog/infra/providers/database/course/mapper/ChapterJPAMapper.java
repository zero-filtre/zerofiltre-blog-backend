package tech.zerofiltre.blog.infra.providers.database.course.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

@Mapper(uses = {CourseJPAMapper.class, LessonJPAMapper.class})
public interface ChapterJPAMapper {

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "number", source = "number.number")
    Chapter toChapter(ChapterJPA chapterJPA);

    @Mapping(target = "course", source = "courseId", qualifiedByName = "courseFromId")
    @Mapping(target = "number", source = "number", qualifiedByName = "fromNumber")
    ChapterJPA toChapterJPA(Chapter chapter);

    @Named("courseFromId")
    default CourseJPA courseFromId(long courseId) {
        CourseJPA courseJPA = new CourseJPA();
        courseJPA.setId(courseId);
        return courseJPA;
    }

    @Named("fromNumber")
    default ChapterJPANumber fromNumber(int number) {
        ChapterJPANumber chapterJPANumber = new ChapterJPANumber();
        chapterJPANumber.setNumber(number);
        return chapterJPANumber;
    }
}
