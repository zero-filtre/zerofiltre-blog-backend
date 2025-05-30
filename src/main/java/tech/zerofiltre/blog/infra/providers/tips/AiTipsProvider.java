package tech.zerofiltre.blog.infra.providers.tips;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.logging.MessageSourceProvider;
import tech.zerofiltre.blog.domain.tips.AiProvider;
import tech.zerofiltre.blog.domain.tips.TipsProvider;
import tech.zerofiltre.blog.infra.InfraProperties;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AiTipsProvider implements TipsProvider {

    private final MessageSourceProvider messageSourceProvider;
    private final AiProvider aiProvider;
    private final InfraProperties infraProperties;

    @Getter
    private String tip;
    @Getter
    private LocalDateTime date;

    @Override
    public String getTip(String profession, Locale locale) throws ZerofiltreException {
        List<String> prompts = new ArrayList<>();
        for (int i = 0; i <= 20; i++) {
            prompts.add(messageSourceProvider.getMessage("message.tip.ai.prompt_" + i, null, locale));
        }
        int promptIndex = RandomUtils.nextInt(0, prompts.size());
        String request = prompts.get(promptIndex);
        return findTip(request);
    }

    String findTip(String request) throws ZerofiltreException {
        if (date != null && date.isAfter(LocalDateTime.now().minusSeconds(infraProperties.getRefreshInterval())) && tip != null && !tip.isBlank())
            return tip;
        tip = aiProvider.answer(request);
        date = LocalDateTime.now();
        return tip;
    }

}
