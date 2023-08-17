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

    //TODO on the next new action, split this class to notify sepecic events

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void notify(UserActionEvent userActionEvent) {
        ApplicationEvent event;
        if (Action.CHECKOUT_STARTED == userActionEvent.getAction()) {

            event = new CheckoutStartedEvent(
                    userActionEvent.getUser(),
                    userActionEvent.getLocale(),
                    userActionEvent.getAppUrl()
            );


        } else if (Action.ARTICLE_SUBMITTED == userActionEvent.getAction()) {
            event = new ArticleSubmittedEvent(
                    userActionEvent.getUser(),
                    userActionEvent.getLocale(),
                    userActionEvent.getAppUrl(),
                    userActionEvent.getArticle()
            );

        } else {
            event = new UserActionApplicationEvent(
                    userActionEvent.getUser(),
                    userActionEvent.getLocale(),
                    userActionEvent.getAppUrl(),
                    userActionEvent.getCurrentToken(),
                    userActionEvent.getAction());

        }
        eventPublisher.publishEvent(event);

    }
}

