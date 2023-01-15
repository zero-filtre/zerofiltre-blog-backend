package tech.zerofiltre.blog.infra.providers.database.course.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.*;

@Mapper(uses = {SectionJPAMapper.class, TagJPAMapper.class, UserJPAMapper.class, ReactionCourseJPAMapper.class})
public interface CourseJPAMapper {

    CourseJPA toJPA(Course course);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Course fromJPA(CourseJPA courseJPA);
}
