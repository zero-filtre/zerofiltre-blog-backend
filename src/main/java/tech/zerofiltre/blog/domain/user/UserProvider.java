package tech.zerofiltre.blog.domain.user;

import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public interface UserProvider {

    Optional<User> userOfId(long userId);

    List<User> users();

    User create(User user);
}
