package tech.zerofiltre.blog.infra.providers.tips;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.logging.MessageSourceProvider;
import tech.zerofiltre.blog.domain.tips.AiProvider;
import tech.zerofiltre.blog.infra.InfraProperties;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiTipsProviderTest {

    private AiTipsProvider aiTipsProvider;

    @Mock
    private MessageSourceProvider messageSourceProvider;

    @Mock
    private AiProvider aiProvider;

    @Mock
    private InfraProperties infraProperties;

    @BeforeEach
    void setUp() {
        aiTipsProvider = new AiTipsProvider(messageSourceProvider, aiProvider, infraProperties);
    }

    @Test
    void given_TipAndDate_areNull_findTip_returns_aNewTip() throws ZerofiltreException {
        //ARRANGE
        when(aiProvider.answer(anyString())).thenReturn("tip");

        //ACT
        aiTipsProvider.findTip("request");

        //ASSERT
        assertThat(aiTipsProvider.getTip()).isEqualTo("tip");
        assertThat(aiTipsProvider.getDate()).isBeforeOrEqualTo(LocalDateTime.now());
        verify(aiProvider, times(1)).answer(anyString());
    }

    @Test
    void given_tipIsNotFresh_findTip_returns_aNewTip() throws ZerofiltreException {
        //ARRANGE
        when(infraProperties.getRefreshInterval()).thenReturn(3600L);
        ReflectionTestUtils.setField(aiTipsProvider, "date", LocalDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(aiTipsProvider, "tip", "a tip");

        when(aiProvider.answer(anyString())).thenReturn("tip");

        //ACT
        aiTipsProvider.findTip("request");

        //ASSERT
        assertThat(aiTipsProvider.getTip()).isEqualTo("tip");
        assertThat(aiTipsProvider.getDate()).isBeforeOrEqualTo(LocalDateTime.now());
        verify(aiProvider, times(1)).answer(anyString());
    }

    @Test
    void given_tipIsFreshButEmpty_findTip_returns_aNewTip() throws ZerofiltreException {
        //ARRANGE
        when(infraProperties.getRefreshInterval()).thenReturn(3600L);
        ReflectionTestUtils.setField(aiTipsProvider, "date", LocalDateTime.now().minusMinutes(10));
        ReflectionTestUtils.setField(aiTipsProvider, "tip", "");

        when(aiProvider.answer(anyString())).thenReturn("tip");

        //ACT
        aiTipsProvider.findTip("request");

        //ASSERT
        assertThat(aiTipsProvider.getTip()).isEqualTo("tip");
        assertThat(aiTipsProvider.getDate()).isBeforeOrEqualTo(LocalDateTime.now());
        verify(aiProvider, times(1)).answer(anyString());
    }

    @Test
    void given_TipIsFresh_findTip_returns_inMemoryTip() throws ZerofiltreException {
        //ARRANGE
        when(infraProperties.getRefreshInterval()).thenReturn(3600L);
        ReflectionTestUtils.setField(aiTipsProvider, "date", LocalDateTime.now().minusMinutes(10));
        ReflectionTestUtils.setField(aiTipsProvider, "tip", "in memory tip");

        //ACT
        String tip = aiTipsProvider.findTip("request");

        //ASSERT
        assertThat(tip).isEqualTo("in memory tip");
        verify(aiProvider, times(0)).answer(anyString());
    }

}