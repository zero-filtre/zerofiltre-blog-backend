package tech.zerofiltre.blog.domain.user;

import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public interface UserProvider {

    Optional<User> userOfId(long userId);

    List<User> users();

    User save(User user);

    Optional<User> userOfEmail(String email);

    List<User> nonActiveUsers();

    void deleteUser(User user);
}
