package tech.zerofiltre.blog.infra.providers.database.article.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tech.zerofiltre.blog.domain.article.model.Reaction;
import tech.zerofiltre.blog.infra.providers.database.article.model.ReactionCourseJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.CourseJPA;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.UserJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;

import java.util.List;
import java.util.Set;

@Mapper(uses = UserJPAMapper.class)
public interface ReactionCourseJPAMapper {

    @Mapping(target = "author", source = "authorId", qualifiedByName = "authorFromId")
    @Mapping(target = "course", source = "courseId", qualifiedByName = "courseFromId")
    ReactionCourseJPA toJPA(Reaction reaction);

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "courseId", source = "course.id")
    Reaction fromJPA(ReactionCourseJPA reactionCourseJPA);

    List<Reaction> fromJPA(Set<ReactionCourseJPA> reactions);

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
