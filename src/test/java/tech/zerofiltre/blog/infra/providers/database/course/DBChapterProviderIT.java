package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import tech.zerofiltre.blog.domain.course.model.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class DBChapterProviderIT {

    DBChapterProvider chapterProvider;

    @Autowired
    ChapterJPANumberRepository chapterJPANumberRepository;
    @Autowired
    ChapterJPARepository chapterJPARepository;

    @BeforeEach
    void setUp() {

        chapterProvider = new DBChapterProvider(chapterJPARepository, chapterJPANumberRepository);
    }

    @Test
    void save_creates_number_if_zero() {
        //given
        Chapter chapter = Chapter.builder()
                .title("title")
                .build();

        //when
        Chapter result = chapterProvider.save(chapter);

        //then
        assertThat(result.getNumber()).isNotZero();

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
