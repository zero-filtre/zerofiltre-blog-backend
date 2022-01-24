package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.notification.user.model.*;

@Component
@RequiredArgsConstructor
public class UserMailNotificationProvider implements UserNotificationProvider {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void notify(UserActionEvent userActionEvent) {
        ApplicationEvent event;
        if (userActionEvent instanceof RegistrationCompleteEvent) {
            event = new OnRegistrationCompleteEvent(
                    userActionEvent.getUser(),
                    userActionEvent.getLocale(),
                    userActionEvent.getAppUrl(),
                    ((RegistrationCompleteEvent) userActionEvent).isRepeated()
            );
        } else {
            event = new OnResetPasswordEvent(
                    userActionEvent.getUser(),
                    userActionEvent.getLocale(),
                    userActionEvent.getAppUrl()
            );
        }

        eventPublisher.publishEvent(event);

    }
}
