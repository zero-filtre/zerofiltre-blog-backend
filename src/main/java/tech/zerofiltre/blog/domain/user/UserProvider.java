package tech.zerofiltre.blog.domain.user;

import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserEmail;

import java.util.List;
import java.util.Optional;

public interface UserProvider {

    Optional<User> userOfId(long userId);

    List<User> users();

    User save(User user);

    Optional<User> userOfEmail(String email);

    List<User> nonActiveUsers();

    void deleteUser(User user);

    Optional<User> userOfSocialId(String userSocialId);

    List<UserEmail> allEmails();
}
