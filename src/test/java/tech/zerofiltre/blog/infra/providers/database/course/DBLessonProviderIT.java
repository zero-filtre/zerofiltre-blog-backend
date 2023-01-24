package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import tech.zerofiltre.blog.domain.course.model.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class DBLessonProviderIT {

    DBLessonProvider lessonProvider;

    @Autowired
    LessonJPARepository lessonJPARepository;

    @Autowired
    LessonJPANumberRepository lessonJPANumberRepository;

    @BeforeEach
    void setUp() {
        lessonProvider = new DBLessonProvider(lessonJPARepository,lessonJPANumberRepository);
    }

    @Test
    void save_creates_number_if_zero() {
        //given
        Lesson lesson = Lesson.builder()
                .title("title")
                .build();

        //when
        Lesson result = lessonProvider.save(lesson);

        //then
        assertThat(result.getNumber()).isNotZero();

    }

    @Test
    void delete_lesson_is_ok() {
        //given
        Lesson lesson = Lesson.builder()
                .title("title")
                .build();
        lesson = lessonProvider.save(lesson);
        assertThat(lessonProvider.lessonOfId(lesson.getId())).isPresent();

        //when
        lessonProvider.delete(lesson);

        //then
        assertThat(lessonProvider.lessonOfId(lesson.getId())).isEmpty();
    }
}
