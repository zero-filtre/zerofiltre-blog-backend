package tech.zerofiltre.blog.infra.providers.database.course.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

@Mapper
public interface SectionJPAMapper {

    @Mapping(target = "course", source = "courseId", qualifiedByName = "courseFromId")
    SectionJPA toJPA(Section section);

    @Mapping(target = "courseId", source = "course.id")
    Section fromJPA(SectionJPA sectionJPA);

    @Named("courseFromId")
    default CourseJPA courseFromId(long courseId) {
        CourseJPA courseJPA = new CourseJPA();
        courseJPA.setId(courseId);
        return courseJPA;
    }
}
