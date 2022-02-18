package tech.zerofiltre.blog.infra.security.filter;

import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.infra.providers.api.so.*;
import tech.zerofiltre.blog.infra.security.model.*;

public class StackOverflowAuthenticationCheckerFilter extends AuthenticationCheckerFilter<StackOverflowAuthenticationTokenProperties, StackOverflowLoginProvider> {


    public StackOverflowAuthenticationCheckerFilter(StackOverflowAuthenticationTokenProperties tokenConfiguration, StackOverflowLoginProvider socialLoginProvider, UserProvider userProvider) {
        super(tokenConfiguration, socialLoginProvider, userProvider);
    }
}
