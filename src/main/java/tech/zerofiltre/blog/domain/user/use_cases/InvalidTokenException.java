package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.*;

public class InvalidTokenException extends BlogException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
