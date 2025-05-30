package tech.zerofiltre.blog.infra.providers.tips;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.logging.MessageSourceProvider;
import tech.zerofiltre.blog.domain.tips.AiProvider;
import tech.zerofiltre.blog.domain.tips.TipsProvider;

import java.time.LocalDateTime;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AiTipsProvider implements TipsProvider {

    private final MessageSourceProvider messageSourceProvider;
    private final AiProvider aiProvider;

    @Getter
    private String tip;
    @Getter
    private LocalDateTime date;

    @Override
    public String getTip(String profession, Locale locale) throws ZerofiltreException {
        String request = messageSourceProvider.getMessage("message.tip.ai.prompt", null, locale);

        if (profession.isEmpty()) {
            return findTip(request + " " + messageSourceProvider.getMessage("message.tip.ai.prompt.profession", null, locale));
        }
        return aiProvider.answer(request + " " + profession);
    }

    String findTip(String request) throws ZerofiltreException {
        if (date != null && date.isAfter(LocalDateTime.now().minusHours(1)) && tip != null && !tip.isBlank())
            return tip;

        tip = aiProvider.answer(request);
        date = LocalDateTime.now();
        return tip;
    }

}
