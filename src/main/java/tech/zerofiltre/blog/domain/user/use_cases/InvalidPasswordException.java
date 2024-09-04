package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.error.ZerofiltreException;

public class InvalidPasswordException extends ZerofiltreException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
