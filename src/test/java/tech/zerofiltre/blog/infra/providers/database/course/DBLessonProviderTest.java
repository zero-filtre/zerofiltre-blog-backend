package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.doubles.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

class DBLessonProviderTest {

    DBLessonProvider dbLessonProvider;



    @Test
    void save_creates_number_if_zero() {
        //given
        LessonJPARepository lessonJPARepository = new DummyLessonJPARepository();
        LessonJPANumberRepository lessonJPANumberRepository = new CreatorLessonJPANumberRepository();
        SubscriptionJPARepository subscriptionJPARepository = mock(SubscriptionJPARepository.class);
        dbLessonProvider = new DBLessonProvider(lessonJPARepository, lessonJPANumberRepository, subscriptionJPARepository);
        Lesson lesson = Lesson.builder()
                .title("title")
                .build();

        //when
        Lesson result = dbLessonProvider.save(lesson);

        //then
        assertThat(result.getNumber()).isEqualTo(1);

    }
}