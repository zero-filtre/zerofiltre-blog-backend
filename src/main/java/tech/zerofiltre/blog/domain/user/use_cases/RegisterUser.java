package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

public class RegisterUser {
    private final UserProvider userProvider;
    private final AvatarProvider avatarProvider;

    public RegisterUser(UserProvider userProvider, AvatarProvider avatarProvider) {
        this.userProvider = userProvider;
        this.avatarProvider = avatarProvider;
    }

    public User execute(User user) throws ResourceAlreadyExistException {
        if (userProvider.userOfEmail(user.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistException("There is an existing account with that email address: " + user.getEmail(), user.getEmail(), Domains.USER.name());
        }
        user.setProfilePicture(avatarProvider.byEmail(user.getEmail()));
        return userProvider.save(user);
    }
}
