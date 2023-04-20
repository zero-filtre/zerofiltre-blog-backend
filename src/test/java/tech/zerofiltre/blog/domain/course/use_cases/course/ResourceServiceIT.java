package tech.zerofiltre.blog.domain.course.use_cases.course;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.article.*;
import tech.zerofiltre.blog.infra.providers.database.course.*;
import tech.zerofiltre.blog.infra.providers.database.user.*;
import tech.zerofiltre.blog.infra.providers.logging.*;
import tech.zerofiltre.blog.util.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
@Import({DBCourseProvider.class, DBUserProvider.class, DBChapterProvider.class, DBLessonProvider.class, DBResourceProvider.class, Slf4jLoggerProvider.class, DBTagProvider.class})
public class ResourceServiceIT {

    private ResourceService resourceService;
    @Autowired
    private ResourceProvider resourceProvider;
    @Autowired
    private LessonProvider lessonProvider;
    @Autowired
    private ChapterProvider chapterProvider;
    @Autowired
    private CourseProvider courseProvider;

    @Autowired
    private UserProvider userProvider;

    @BeforeEach
    void setUp() {
        resourceService = new ResourceService(resourceProvider, lessonProvider, chapterProvider, courseProvider);
    }

    @Test
    void should_create_resource() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Chapter chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        Lesson lesson = Lesson.builder()
                .chapterId(chapter.getId())
                .title("title")
                .build();
        lesson = lessonProvider.save(lesson);

        Resource resource = Resource.builder()
                .name("name")
                .url("url")
                .lessonId(lesson.getId())
                .build();

        //when
        Resource savedResource = resourceService.createResource(resource, user);

        //then
        assertThat(savedResource.getId()).isNotZero();
    }


    @Test
    void should_delete_resource() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Chapter chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        Lesson lesson = Lesson.builder()
                .chapterId(chapter.getId())
                .title("title")
                .build();
        lesson = lessonProvider.save(lesson);

        Resource resource = Resource.builder()
                .name("name")
                .url("url")
                .lessonId(lesson.getId())
                .build();
        resource = resourceProvider.save(resource);
        assertThat(resource.getId()).isNotZero();

        //when
        resourceService.deleteResource(resource.getId(), user);

        //then
        Optional<Resource> notFoundResource = resourceProvider.resourceOfId(resource.getId());
        assertThat(notFoundResource).isEmpty();
    }


}
