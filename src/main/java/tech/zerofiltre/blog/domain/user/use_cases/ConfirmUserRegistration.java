package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

public class ConfirmUserRegistration {
    public static final String TOKEN_EXPIRED = "The token you've provided is expired";
    public static final String INVALID_TOKEN = "We couldn't find a token corresponding to the one you've provided";
    private final VerifyToken verifyToken;

    private final UserProvider userProvider;


    public ConfirmUserRegistration(VerificationTokenProvider verificationTokenProvider, UserProvider userProvider) {
        this.userProvider = userProvider;
        verifyToken = new VerifyToken(verificationTokenProvider);
    }

    public User execute(String token) throws InvalidTokenException {
        VerificationToken verificationToken = verifyToken.execute(token);
        User user = verificationToken.getUser();
        user.setActive(true);
        User result = userProvider.save(user);
        verifyToken.invalidate(verificationToken);
        return result;
    }
}
