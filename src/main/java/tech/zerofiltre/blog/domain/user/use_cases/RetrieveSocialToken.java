package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;

public class RetrieveSocialToken {

    private final SocialLoginProvider socialLoginProvider;

    public RetrieveSocialToken(SocialLoginProvider socialLoginProvider) {
        this.socialLoginProvider = socialLoginProvider;
    }

    public String execute(String code) throws ResourceNotFoundException {
        String token = socialLoginProvider.tokenFromCode(code);
        if (token == null)
            throw new ResourceNotFoundException("We couldn't retrieve the token with the code you've provided: " + code, code, Domains.NONE.name());
        return token;
    }
}
