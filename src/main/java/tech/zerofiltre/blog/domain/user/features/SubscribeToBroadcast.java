package tech.zerofiltre.blog.domain.user.features;

import lombok.RequiredArgsConstructor;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

@RequiredArgsConstructor
public class SubscribeToBroadcast {

    private final UserProvider userProvider;

    public String execute(User user, boolean subscribe) {
        user.setSubscribedToBroadcast(subscribe);
        userProvider.save(user);
        return "subscribe = " + subscribe;
    }
}
