package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

public class UpdateUser {

    private final UserProvider userProvider;

    public UpdateUser(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    public User patch(User currentUser, User user) throws ForbiddenActionException, UserNotFoundException {

        if (currentUser.getId() != user.getId() && !currentUser.getRoles().contains("ROLE_ADMIN"))
            throw new ForbiddenActionException("You can only save your own account", Domains.USER.name());

        User foundUser = userProvider.userOfId(user.getId())
                .orElseThrow(() -> new UserNotFoundException("We could not find the user you want to save", String.valueOf(user.getId())));

        foundUser.setFullName(user.getFullName());
        foundUser.setProfilePicture(user.getProfilePicture());
        foundUser.setProfession(user.getProfession());
        foundUser.setBio(user.getBio());
        foundUser.setLanguage(user.getLanguage());
        foundUser.setWebsite(user.getWebsite());
        foundUser.setSocialLinks(user.getSocialLinks());
        userProvider.save(foundUser);
        return foundUser;
    }
}
