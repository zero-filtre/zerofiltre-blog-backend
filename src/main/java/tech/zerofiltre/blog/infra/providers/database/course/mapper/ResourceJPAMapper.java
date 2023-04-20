package tech.zerofiltre.blog.infra.providers.database.course.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

@Mapper
public interface ResourceJPAMapper {

    @Mapping(target = "lesson", source = "lessonId", qualifiedByName = "lessonFromId")
    ResourceJPA toJPA(Resource resource);

    @Mapping(target = "lessonId", source = "lesson.id")
    Resource fromJPA(ResourceJPA resourceJPA);

    @Named("lessonFromId")
    default LessonJPA lessonFromId(long lessonId) {
        LessonJPA lessonJPA = new LessonJPA();
        lessonJPA.setId(lessonId);
        return lessonJPA;
    }
}
