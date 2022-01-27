package tech.zerofiltre.blog.domain.user;

import tech.zerofiltre.blog.domain.user.model.*;

public interface UserNotificationProvider {

    void notify(UserActionEvent userActionEvent);
}
