package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.*;

public class InvalidPasswordException extends BlogException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
