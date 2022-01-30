package tech.zerofiltre.blog.domain.user;

import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;

public interface StackOverflowProvider {
    boolean isValid(String token);

    Optional<User> userOfToken(String token);
}
