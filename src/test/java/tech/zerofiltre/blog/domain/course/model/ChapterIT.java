package tech.zerofiltre.blog.domain.course.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.*;
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
class ChapterIT {

    public static final String TITLE = "Chapter 1";
    public static final String ANOTHER_TITLE = "Chapter 2";
    public static final String UPDATED_TITLE = "updated title";
    private Chapter chapter;
    private User author;
    private Course course;
    @Autowired
    private CourseProvider courseProvider;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private ChapterProvider chapterProvider;

    @Autowired
    private LessonProvider lessonProvider;

    @Test
    void init_chapter_is_OK() throws ForbiddenActionException, ResourceNotFoundException {
        author = ZerofiltreUtils.createMockUser(false);
        author.setEmail("test@mail.uk");
        author.setPseudoName("pseudo");
        author = userProvider.save(author);

        course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
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
        Assertions.assertThat(chapter.getNumber()).isOne();
        Assertions.assertThat(chapter.getLessons()).isEmpty();

    }

    @Test
    void save_chapter_is_OK() throws BlogException {
        author = ZerofiltreUtils.createMockUser(false);
        author.setEmail("save_chapter_is_OK@mail.uk");
        author.setPseudoName("save_chapter_is_OK");
        author = userProvider.save(author);

        course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = Chapter.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .build()
                .init(TITLE, course.getId(), author.getId());

        Chapter savedChapter = Chapter.builder()
                .id(chapter.getId())
                .title(UPDATED_TITLE)
                .courseId(chapter.getCourseId())
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .number(54)
                .lessons(Collections.singletonList(Lesson.builder().build()))
                .build()
                .save(author.getId());

        Assertions.assertThat(savedChapter).isNotNull();
        Assertions.assertThat(savedChapter.getId()).isNotZero();
        Assertions.assertThat(savedChapter.getTitle()).isEqualTo(UPDATED_TITLE);
        Assertions.assertThat(savedChapter.getCourseId()).isEqualTo(course.getId());
        Assertions.assertThat(savedChapter.getNumber()).isOne();

    }

    @Test
    void delete_chapter_is_OK() throws BlogException {
        author = ZerofiltreUtils.createMockUser(false);
        author.setEmail("delete_chapter_is_OK@mail.uk");
        author.setPseudoName("delete_chapter_is_OK");
        author = userProvider.save(author);

        course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = Chapter.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .build()
                .init(TITLE, course.getId(), author.getId());
        assertThat(chapter.getId()).isNotZero();

        chapter.delete(author.getId());

        Assertions.assertThat(chapterProvider.chapterOfId(chapter.getId())).isEmpty();
    }

    @Test
    void getAllChaptersByCourseId_isOK() throws ForbiddenActionException, ResourceNotFoundException {
        author = ZerofiltreUtils.createMockUser(false);
        author.setEmail("getAllChaptersByCourseId_isOK@mail.uk");
        author.setPseudoName("getAllChaptersByCourseId_isOK");
        author = userProvider.save(author);

        course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = Chapter.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .build()
                .init(TITLE, course.getId(), author.getId());

        chapter = Chapter.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .build()
                .init(ANOTHER_TITLE, course.getId(), author.getId());

        List<Chapter> chapters = chapter.getByCourseId(author);
        Assertions.assertThat(chapters).isNotEmpty();
        assertThat(chapters.size()).isEqualTo(2);
        Chapter chapter1 = chapters.get(0);
        Chapter chapter2 = chapters.get(1);
        assertThat(chapter1.getTitle()).isEqualTo(TITLE);
        assertThat(chapter2.getTitle()).isEqualTo(ANOTHER_TITLE);
        assertThat(chapter1.getNumber()).isLessThan(chapter2.getNumber());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void moveLessonUPIsOk() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        author = ZerofiltreUtils.createMockUser(false);
        author.setEmail("moveLessonUPIsOk@mail.uk");
        author.setPseudoName("moveLessonUPIsOk");
        author = userProvider.save(author);

        course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = Chapter.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init(TITLE, course.getId(), author.getId());


        Lesson lesson1 = Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init(TITLE, chapter.getId(), author.getId());

        Lesson lesson2 = Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init("title1", chapter.getId(), author.getId());

        Lesson lesson3 = Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init("title2", chapter.getId(), author.getId());

        chapter = chapter.get();

        List<Lesson> lessons = chapter.getLessons();
        assertThat(lessons.size()).isEqualTo(3);


        lessons.forEach(lesson -> {
            if (lesson.getId() == lesson1.getId()) assertThat(lesson.getNumber()).isEqualTo(1);
            if (lesson.getId() == lesson2.getId()) assertThat(lesson.getNumber()).isEqualTo(2);
            if (lesson.getId() == lesson3.getId()) assertThat(lesson.getNumber()).isEqualTo(3);
        });

        //when
        chapter = chapter.moveLesson(author.getId(), lesson3.getId(), 2);

        //then
        List<Lesson> reOrderedLessons = chapter.getLessons();
        assertThat(reOrderedLessons.size()).isEqualTo(3);

        // Check that the moved lesson is now in position 2
        reOrderedLessons.forEach(lesson -> {
            if (lesson.getId() == lesson1.getId()) assertThat(lesson.getNumber()).isEqualTo(1);
            if (lesson.getId() == lesson2.getId()) assertThat(lesson.getNumber()).isEqualTo(3);
            if (lesson.getId() == lesson3.getId()) assertThat(lesson.getNumber()).isEqualTo(2);
        });

    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void moveLessonDownIsOk() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = Chapter.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init(TITLE, course.getId(), author.getId());


        Lesson lesson1 = Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init(TITLE, chapter.getId(), author.getId());

        Lesson lesson2 = Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init("title1", chapter.getId(), author.getId());

        Lesson lesson3 = Lesson.builder()
                .courseProvider(courseProvider)
                .userProvider(userProvider)
                .chapterProvider(chapterProvider)
                .lessonProvider(lessonProvider)
                .build()
                .init("title2", chapter.getId(), author.getId());

        chapter = chapter.get();

        List<Lesson> lessons = chapter.getLessons();
        assertThat(lessons.size()).isEqualTo(3);


        lessons.forEach(lesson -> {
            if (lesson.getId() == lesson1.getId()) assertThat(lesson.getNumber()).isEqualTo(1);
            if (lesson.getId() == lesson2.getId()) assertThat(lesson.getNumber()).isEqualTo(2);
            if (lesson.getId() == lesson3.getId()) assertThat(lesson.getNumber()).isEqualTo(3);
        });

        //when
        chapter = chapter.moveLesson(author.getId(), lesson1.getId(), 2);

        //then
        List<Lesson> reOrderedLessons = chapter.getLessons();
        assertThat(reOrderedLessons.size()).isEqualTo(3);

        // Check that the moved lesson is now in position 2
        reOrderedLessons.forEach(lesson -> {
            if (lesson.getId() == lesson1.getId()) assertThat(lesson.getNumber()).isEqualTo(2);
            if (lesson.getId() == lesson2.getId()) assertThat(lesson.getNumber()).isEqualTo(1);
            if (lesson.getId() == lesson3.getId()) assertThat(lesson.getNumber()).isEqualTo(3);
        });

    }


}