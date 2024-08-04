package tech.zerofiltre.blog.infra.providers.database.course.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tech.zerofiltre.blog.domain.course.model.Review;
import tech.zerofiltre.blog.infra.providers.database.course.model.ChapterJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.CourseJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.ReviewJPA;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;

@Mapper(uses = {CourseJPAMapper.class, ChapterJPAMapper.class})
public interface ReviewJPAMapper {

    @Mapping(target = "chapter", source = "chapterId", qualifiedByName = "ReviewJPAMapper_chapterFromId")
    @Mapping(target = "user", source = "reviewAuthorId", qualifiedByName = "ReviewJPAMapper_userFromId")
    ReviewJPA toJPA(Review review);

    @Mapping(target = "chapterId", source = "chapter.id")
    @Mapping(target = "reviewAuthorId", source = "user.id")
    Review fromJPA(ReviewJPA reviewJPA);

    @Named("ReviewJPAMapper_courseFromId")
    default CourseJPA courseFromId(long courseId) {
        CourseJPA courseJPA = new CourseJPA();
        courseJPA.setId(courseId);
        return courseJPA;
    }

    @Named("ReviewJPAMapper_chapterFromId")
    default ChapterJPA chapterFromId(long chapterId) {
        ChapterJPA chapterJPA = new ChapterJPA();
        chapterJPA.setId(chapterId);
        return chapterJPA;
    }

    @Named("ReviewJPAMapper_userFromId")
    default UserJPA userFromId(long userId) {
        UserJPA userJPA = new UserJPA();
        userJPA.setId(userId);
        return userJPA;
    }
}
