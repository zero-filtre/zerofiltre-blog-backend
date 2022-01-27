package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

public class SavePasswordReset {

    private final VerifyToken verifyToken;
    private final UserProvider userProvider;


    public SavePasswordReset(VerificationTokenProvider verificationTokenProvider, UserProvider userProvider) {
        this.verifyToken = new VerifyToken(verificationTokenProvider);
        this.userProvider = userProvider;
    }

    public void execute(String encodedPassword, String token) throws InvalidTokenException {
        VerificationToken verificationToken = verifyToken.execute(token);
        User user = verificationToken.getUser();
        user.setPassword(encodedPassword);
        userProvider.save(user);
    }


}
