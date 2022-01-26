package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

public class UpdatePassword {

    private final UserProvider userProvider;
    private final PasswordVerifierProvider passwordVerifierProvider;

    public UpdatePassword(UserProvider userProvider, PasswordVerifierProvider passwordVerifierProvider) {
        this.userProvider = userProvider;
        this.passwordVerifierProvider = passwordVerifierProvider;
    }

    public void execute(String email, String oldPassword, String newEncodedPassword) throws UserNotFoundException, InvalidPasswordException {
        User userFromEmail = userProvider.userOfEmail(email)
                .orElseThrow(() -> new UserNotFoundException("We were unable to find a connected user"));

        if (!passwordVerifierProvider.isValid(userFromEmail, oldPassword)) {
            throw new InvalidPasswordException("The password provided does not match the current user");
        }
        userFromEmail.setPassword(newEncodedPassword);
        userProvider.save(userFromEmail);


    }
}
