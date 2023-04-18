package tech.zerofiltre.blog.infra.providers.database.article.mapper;

import org.mapstruct.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

@Mapper(uses = UserJPAMapper.class)
public interface ReactionCourseJPAMapper {

    @Mapping(target = "author", source = "authorId", qualifiedByName = "authorFromId")
    @Mapping(target = "course", source = "courseId", qualifiedByName = "courseFromId")
    public abstract ReactionCourseJPA toJPA(Reaction reaction);

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "courseId", source = "course.id")
    public abstract Reaction fromJPA(ReactionCourseJPA reactionCourseJPA);

    @Named("authorFromId")
    default UserJPA authorFromId(long authorId) {
        UserJPA userJPA = new UserJPA();
        userJPA.setId(authorId);
        return userJPA;
    }

    @Named("courseFromId")
    default CourseJPA courseFromId(long courseId) {
        CourseJPA courseJPA = new CourseJPA();
        courseJPA.setId(courseId);
        return courseJPA;
    }
}
