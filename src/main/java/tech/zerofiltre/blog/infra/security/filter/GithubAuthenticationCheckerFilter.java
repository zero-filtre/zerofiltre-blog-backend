package tech.zerofiltre.blog.infra.security.filter;

import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.infra.providers.api.github.*;
import tech.zerofiltre.blog.infra.security.model.*;

public class GithubAuthenticationCheckerFilter extends AuthenticationCheckerFilter<GithubAuthenticationTokenProperties, GithubLoginProvider> {


    public GithubAuthenticationCheckerFilter(GithubAuthenticationTokenProperties tokenConfiguration, GithubLoginProvider socialLoginProvider, UserProvider userProvider) {
        super(tokenConfiguration, socialLoginProvider, userProvider);
    }
}
