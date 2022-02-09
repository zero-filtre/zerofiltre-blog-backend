package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.error.*;

public class InvalidPasswordException extends BlogException {
    public InvalidPasswordException(String message) {
        super(message, Domains.USER.name());
    }
}
