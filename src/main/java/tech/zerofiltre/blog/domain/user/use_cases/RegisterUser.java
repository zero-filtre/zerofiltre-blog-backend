package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;

public class RegisterUser {
    private final UserProvider userProvider;

    public RegisterUser(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    public User execute(User user) throws UserAlreadyExistException {
        if (userProvider.userOfEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistException("There is an existing account with that email address: " + user.getEmail(), user.getEmail());
        }
        user.setRegisteredOn(LocalDateTime.now());
        return userProvider.save(user);
    }
}
