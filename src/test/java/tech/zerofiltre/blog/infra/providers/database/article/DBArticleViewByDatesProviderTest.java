package tech.zerofiltre.blog.infra.providers.database.article;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class DBArticleViewByDatesProviderTest {

    @Mock
    ArticleViewJPARepository articleViewJPARepository;

    @Test
    void givenDateStartIsValidAndDateEndIsValidAndUserIdIsZero_whenCountByUser_ThenCall_countByDateStartAndDateEndAndViewerId() {

        Mockito.when(articleViewJPARepository.countByDateStartAndDateEndAndViewerId(any(), any(), any())).thenReturn(any());

        DBArticleViewByDatesProvider dbArticleViewByDatesProvider = new DBArticleViewByDatesProvider(articleViewJPARepository);
        dbArticleViewByDatesProvider.countByUser("2024-05-01", "2024-06-01", 0);

        Mockito.verify(articleViewJPARepository, Mockito.times(1)).countByDateStartAndDateEndAndViewerId(any(), any(), any());

    }

}
