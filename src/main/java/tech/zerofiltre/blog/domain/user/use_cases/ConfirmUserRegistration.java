package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;

public class ConfirmUserRegistration {
    public static final String TOKEN_EXPIRED = "The token you've provided is expired";
    public static final String INVALID_TOKEN = "We couldn't find a token corresponding to the one you've provided";
    private final VerificationTokenProvider verificationTokenProvider;
    private final UserProvider userProvider;


    public ConfirmUserRegistration(VerificationTokenProvider verificationTokenProvider, UserProvider userProvider) {
        this.verificationTokenProvider = verificationTokenProvider;
        this.userProvider = userProvider;
    }

    public User execute(String token) throws InvalidTokenException {
        VerificationToken verificationToken = verificationTokenProvider.ofToken(token)
                .orElseThrow(() -> new InvalidTokenException(INVALID_TOKEN));

        User user = verificationToken.getUser();
        if(Duration.between(LocalDateTime.now(),verificationToken.getExpiryDate()).isNegative()){
            throw  new InvalidTokenException(TOKEN_EXPIRED);
        }
        user.setActive(true);
        return userProvider.save(user);

    }
}
