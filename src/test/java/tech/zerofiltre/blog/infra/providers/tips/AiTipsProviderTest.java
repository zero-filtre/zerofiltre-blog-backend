package tech.zerofiltre.blog.infra.providers.tips;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.logging.MessageSourceProvider;
import tech.zerofiltre.blog.domain.tips.AiProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiTipsProviderTest {

    private AiTipsProvider aiTipsProvider;

    @Mock
    private MessageSourceProvider messageSourceProvider;

    @Mock
    private AiProvider aiProvider;

    @BeforeEach
    void setUp() {
        aiTipsProvider = new AiTipsProvider(messageSourceProvider, aiProvider);
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
    void given_tipIsFromYesterday_findTip_returns_aNewTip() throws ZerofiltreException {
        //ARRANGE
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
        ReflectionTestUtils.setField(aiTipsProvider, "date", LocalDateTime.now().minusMinutes(10));
        ReflectionTestUtils.setField(aiTipsProvider, "tip", "in memory tip");

        //ACT
        String tip = aiTipsProvider.findTip("request");

        //ASSERT
        assertThat(tip).isEqualTo("in memory tip");
        verify(aiProvider, times(0)).answer(anyString());
    }

    @DisplayName("")
    @Test
    void given_profession_is_empty_getTips_callsFindTips() throws ZerofiltreException {
        //ARRANGE
        String profession = "";
        Locale locale = Locale.FRENCH;

        ReflectionTestUtils.setField(aiTipsProvider, "date", LocalDateTime.now().minusMinutes(120));
        ReflectionTestUtils.setField(aiTipsProvider, "tip", "in memory tip");
        when(aiProvider.answer(anyString())).thenReturn("tip");

        when(messageSourceProvider.getMessage(anyString(), eq(null), any(Locale.class))).thenReturn("question").thenReturn("Dev ou DevOps");

        //ACT
        String tip = aiTipsProvider.getTip(profession, locale);

        //ASSERT
        assertThat(tip).isEqualTo("tip");
        verify(messageSourceProvider, times(2)).getMessage(anyString(), eq(null), any(Locale.class));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(aiProvider, times(1)).answer(captor.capture());
        String questionCaptor = captor.getValue();
        assertThat(questionCaptor).isEqualTo("question Dev ou DevOps");
    }

    @Test
    void given_profession_is_Notempty_getTips_getsANewTip() throws ZerofiltreException {
        //ARRANGE
        String profession = "engineer";
        Locale locale = Locale.FRENCH;

        when(messageSourceProvider.getMessage(anyString(), eq(null), any(Locale.class))).thenReturn("question");
        when(aiProvider.answer(anyString())).thenReturn("tip");

        //ACT
        String tip = aiTipsProvider.getTip(profession, locale);

        //ASSERT
        assertThat(tip).isEqualTo("tip");
        verify(messageSourceProvider, times(1)).getMessage(anyString(), eq(null), any(Locale.class));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(aiProvider, times(1)).answer(captor.capture());
        String questionCaptor = captor.getValue();
        assertThat(questionCaptor).isEqualTo("question " + profession);
    }
}