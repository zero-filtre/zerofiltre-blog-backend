package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.*;

public class UserAlreadyExistException extends BlogException {

    private final String email;

    public UserAlreadyExistException(String message, String email) {
        super(message);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
