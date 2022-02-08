package tech.zerofiltre.blog.infra.security.filter;

import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.infra.providers.api.so.*;
import tech.zerofiltre.blog.infra.security.model.*;

public class StackOverflowAuthenticationFilter extends AuthenticationFilter<StackOverflowAuthenticationToken, StackOverflowLoginProvider> {


    public StackOverflowAuthenticationFilter(StackOverflowAuthenticationToken tokenConfiguration, StackOverflowLoginProvider socialLoginProvider, UserProvider userProvider) {
        super(tokenConfiguration, socialLoginProvider, userProvider);
    }
}
