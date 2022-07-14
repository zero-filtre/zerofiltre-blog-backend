package tech.zerofiltre.blog.domain.user;

import tech.zerofiltre.blog.domain.user.model.*;

public interface PasswordVerifierProvider {
    boolean isValid(User userFromEmail, String encodedPassword);
}
