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

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
@Import({DBCourseProvider.class, DBUserProvider.class, DBChapterProvider.class, DBLessonProvider.class})
class LessonIT {

    public static final String TITLE = "Lesson 1";
    public static final String UPDATED_TITLE = "updated title";
    public static final String VIDEO = "https://www.youtube.com/watch?v=QH2-TGUlwu4";
    public static final String UPDATED_TYPE = "updated type";
    public static final String UPDATED_CONTENT = "updated content";
    private static final String UPDATED_SUMMARY = "updated summary";
    public static final String THUMBNAIL = "https://img.bb.com/THU";

    @Autowired
    private LessonProvider lessonProvider;
    @Autowired
    private ChapterProvider chapterProvider;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private CourseProvider courseProvider;

    private Chapter chapter;
    private User author;
    private Course course;
    private Lesson lesson;


    @Test
    void init_lesson_is_OK() throws ForbiddenActionException, ResourceNotFoundException {

        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, courseProvider, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        lesson = Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init(TITLE, chapter.getId(), author.getId());

        org.assertj.core.api.Assertions.assertThat(lesson).isNotNull();
        org.assertj.core.api.Assertions.assertThat(lesson.getId()).isNotZero();
        org.assertj.core.api.Assertions.assertThat(lesson.getTitle()).isEqualTo(TITLE);
        org.assertj.core.api.Assertions.assertThat(lesson.getChapterId()).isEqualTo(course.getId());
        Assertions.assertThat(chapter.getLessons()).isEmpty();

    }

    @Test
    void save_lesson_is_OK() throws ForbiddenActionException, ResourceNotFoundException {

        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, courseProvider, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        lesson = Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init(TITLE, chapter.getId(), author.getId());

        Lesson updatedLesson = Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .id(lesson.getId())
                .title(UPDATED_TITLE)
                .video(VIDEO)
                .type(UPDATED_TYPE)
                .content(UPDATED_CONTENT)
                .summary(UPDATED_SUMMARY)
                .thumbnail(THUMBNAIL)
                .resources(Collections.singletonList(Resource.builder().name("name").build()))
                .free(true)
                .chapterId(chapter.getId())
                .build()
                .save(author.getId());

        org.assertj.core.api.Assertions.assertThat(updatedLesson).isNotNull();
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getId()).isNotZero();
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getTitle()).isEqualTo(UPDATED_TITLE);
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getContent()).isEqualTo(UPDATED_CONTENT);
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getSummary()).isEqualTo(UPDATED_SUMMARY);
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getThumbnail()).isEqualTo(THUMBNAIL);
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getType()).isEqualTo(UPDATED_TYPE);
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getVideo()).isEqualTo(VIDEO);
        org.assertj.core.api.Assertions.assertThat(updatedLesson.isFree()).isTrue();
        org.assertj.core.api.Assertions.assertThat(updatedLesson.getChapterId()).isEqualTo(chapter.getId());
        Assertions.assertThat(lesson.getResources()).isEmpty();

    }

    @Test
    void delete_lesson_isOk() throws ForbiddenActionException, ResourceNotFoundException {
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, courseProvider, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        lesson = Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init(TITLE, chapter.getId(), author.getId());

        assertThat(lessonProvider.lessonOfId(lesson.getId())).isNotEmpty();


        lesson.delete(author.getId());

        assertThat(lessonProvider.lessonOfId(lesson.getId())).isEmpty();

    }
}
