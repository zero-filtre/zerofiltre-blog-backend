package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.*;

public class UserNotFoundException extends BlogException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
