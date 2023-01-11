package tech.zerofiltre.blog.domain.course.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

@DataJpaTest
@Import({DBCourseProvider.class, DBUserProvider.class, DBChapterProvider.class})
class ChapterIT {

    public static final String TITLE = "Chapter 1";
    private Chapter chapter;
    private User author;
    private Course course;
    @Autowired
    private CourseProvider courseProvider;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private ChapterProvider chapterProvider;

    @Test
    void save_chapter_is_OK() throws ForbiddenActionException, ResourceNotFoundException {
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, courseProvider, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);


        chapter = Chapter.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .build()
                .init(TITLE, course.getId(), author.getId());

        Assertions.assertThat(chapter).isNotNull();
        Assertions.assertThat(chapter.getId()).isNotZero();
        Assertions.assertThat(chapter.getTitle()).isEqualTo(TITLE);
        Assertions.assertThat(chapter.getCourseId()).isEqualTo(course.getId());
        Assertions.assertThat(chapter.getLessons()).isEmpty();

    }
}
