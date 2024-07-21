package tech.zerofiltre.blog.infra.providers.database.course.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.sandbox.model.Sandbox;
import tech.zerofiltre.blog.infra.providers.database.article.mapper.ReactionCourseJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.article.mapper.TagJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.course.model.CourseJPA;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.UserJPAMapper;

@Mapper(uses = {SectionJPAMapper.class, TagJPAMapper.class, UserJPAMapper.class, ReactionCourseJPAMapper.class})
public abstract class CourseJPAMapper {


    private final TagJPAMapper tagJPAMapper = Mappers.getMapper(TagJPAMapper.class);
    private final ReactionCourseJPAMapper reactionMapper = Mappers.getMapper(ReactionCourseJPAMapper.class);

    public abstract CourseJPA toJPA(Course course);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract Course fromJPA(CourseJPA courseJPA);

    @Named("fromJPALight")
    public Course fromJPALight(CourseJPA courseJPA) {
        if (courseJPA == null) {
            return null;
        }

        Course course = new Course();

        course.setId(courseJPA.getId());
        course.setTitle(courseJPA.getTitle());
        course.setSubTitle(courseJPA.getSubTitle());
        course.setSummary(courseJPA.getSummary());
        course.setThumbnail(courseJPA.getThumbnail());
        course.setPrice(courseJPA.getPrice());
        course.setCreatedAt(courseJPA.getCreatedAt());
        course.setPublishedAt(courseJPA.getPublishedAt());
        course.setLastPublishedAt(courseJPA.getLastPublishedAt());
        course.setLastSavedAt(courseJPA.getLastSavedAt());
        if (courseJPA.getSandboxType() != null) {
            course.setSandboxType(Enum.valueOf(Sandbox.Type.class, courseJPA.getSandboxType()));
        }
        course.setTags(tagJPAMapper.fromJPA(courseJPA.getTags()));
        course.setEnrolledCount(courseJPA.getEnrolledCount());
        course.setStatus(courseJPA.getStatus());
        course.setVideo(courseJPA.getVideo());

        course.setReactions(reactionMapper.fromJPAListLight(courseJPA.getReactions()));
        course.setMentored(courseJPA.isMentored());

        return course;
    }
}
