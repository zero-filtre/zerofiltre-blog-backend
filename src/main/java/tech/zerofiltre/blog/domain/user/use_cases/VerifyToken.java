package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;

public class VerifyToken {
    public static final String TOKEN_EXPIRED = "The token you've provided is expired";
    public static final String INVALID_TOKEN = "We couldn't find a token corresponding to the one you've provided";
    private final VerificationTokenProvider verificationTokenProvider;


    public VerifyToken(VerificationTokenProvider verificationTokenProvider) {
        this.verificationTokenProvider = verificationTokenProvider;
    }

    public VerificationToken execute(String token) throws InvalidTokenException {
        VerificationToken verificationToken = verificationTokenProvider.ofToken(token)
                .orElseThrow(() -> new InvalidTokenException(INVALID_TOKEN));

        if (Duration.between(LocalDateTime.now(), verificationToken.getExpiryDate()).isNegative()) {
            throw new InvalidTokenException(TOKEN_EXPIRED);
        }
        return verificationToken;
    }
}
