package tech.zerofiltre.blog.infra.security.filter;

import tech.zerofiltre.blog.infra.providers.api.so.*;
import tech.zerofiltre.blog.infra.security.model.*;

public class StackOverflowAuthenticationCheckerFilter extends AuthenticationCheckerFilter<StackOverflowAuthenticationTokenProperties, StackOverflowLoginProvider> {

    public StackOverflowAuthenticationCheckerFilter(StackOverflowAuthenticationTokenProperties tokenConfiguration, SocialTokenValidatorAndAuthenticator<StackOverflowLoginProvider> socialTokenValidatorAndAuthenticator) {
        super(tokenConfiguration, socialTokenValidatorAndAuthenticator);
    }
}
