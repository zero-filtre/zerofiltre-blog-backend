package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

public class RegisterUser {
    private final UserProvider userProvider;

    public RegisterUser(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    public User execute(User user) throws ResourceAlreadyExistException {
        if (userProvider.userOfEmail(user.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistException("There is an existing account with that email address: " + user.getEmail(), user.getEmail(), Domains.USER.name());
        }
        return userProvider.save(user);
    }
}
