package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import tech.zerofiltre.blog.domain.course.model.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DataJpaTest
class DBChapterProviderIT {

    DBChapterProvider chapterProvider;

    @Autowired
    ChapterJPARepository chapterJPARepository;

    @BeforeEach
    void setUp() {

        chapterProvider = new DBChapterProvider(chapterJPARepository);
    }


    @Test
    void delete_chapter_is_ok() {
        //given
        Chapter chapter = Chapter.builder()
                .title("title")
                .build();
        chapter = chapterProvider.save(chapter);
        assertThat(chapterProvider.chapterOfId(chapter.getId())).isPresent();

        //when
        chapterProvider.delete(chapter);

        //then
        assertThat(chapterProvider.chapterOfId(chapter.getId())).isEmpty();
    }

}
