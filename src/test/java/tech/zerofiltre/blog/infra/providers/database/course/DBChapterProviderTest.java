package tech.zerofiltre.blog.infra.providers.database.course;

import org.junit.jupiter.api.*;
import org.mapstruct.factory.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DBChapterProviderTest {

    DBChapterProvider dbChapterProvider;


    @Test
    void save_create_number_if_zero() {
        //given
        ChapterJPARepository chapterJPARepository = new DummyChapterJPARepository();
        ChapterJPANumberRepository chapterJPANumberRepository = new CreatorChapterJPANumberRepository();
        dbChapterProvider = new DBChapterProvider(chapterJPARepository, chapterJPANumberRepository);
        Chapter chapter = Chapter.builder()
                .title("title")
                .build();
        //when
        Chapter result = dbChapterProvider.save(chapter);

        //then
        assertThat(result.getNumber()).isEqualTo(1);

    }
}