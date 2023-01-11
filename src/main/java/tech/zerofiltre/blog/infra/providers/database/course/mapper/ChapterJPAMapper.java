package tech.zerofiltre.blog.infra.providers.database.course.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

@Mapper(uses = {CourseJPAMapper.class, LessonJPAMapper.class})
public interface ChapterJPAMapper {

    @Mapping(target = "courseId", source = "course.id")
    Chapter toChapter(ChapterJPA chapterJPA);

    @Mapping(target = "course", source = "courseId", qualifiedByName = "courseFromId")
    ChapterJPA toChapterJPA(Chapter chapter);

    @Named("courseFromId")
    default CourseJPA courseFromId(long courseId) {
        CourseJPA courseJPA = new CourseJPA();
        courseJPA.setId(courseId);
        return courseJPA;
    }
}
