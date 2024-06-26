package tech.zerofiltre.blog.infra.providers.database.article;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class DBArticleByDatesProviderTest {

    @Mock
    ArticleJPARepository articleJPARepository;

    @Test
    void givenDateStartIsValidAndDateEndIsValidAndUserIdIsZero_whenCountByUser_ThenCall_countByDateStartAndDateEndAndAuthorId() {

        Mockito.when(articleJPARepository.countByDateStartAndDateEndAndAuthorId(any(), any(), any())).thenReturn(any());

        DBArticleByDatesProvider dbArticleByDatesProvider = new DBArticleByDatesProvider(articleJPARepository);
        dbArticleByDatesProvider.countByUser("2024-05-01", "2024-06-01", 0);

        Mockito.verify(articleJPARepository, Mockito.times(1)).countByDateStartAndDateEndAndAuthorId(any(), any(), any());

    }

}
