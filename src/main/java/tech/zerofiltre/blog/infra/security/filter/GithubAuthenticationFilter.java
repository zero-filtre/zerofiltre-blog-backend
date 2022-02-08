package tech.zerofiltre.blog.infra.security.filter;

import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.infra.providers.api.github.*;
import tech.zerofiltre.blog.infra.security.model.*;

public class GithubAuthenticationFilter extends AuthenticationFilter<GithubAuthenticationToken, GithubLoginProvider> {


    public GithubAuthenticationFilter(GithubAuthenticationToken tokenConfiguration, GithubLoginProvider socialLoginProvider, UserProvider userProvider) {
        super(tokenConfiguration, socialLoginProvider, userProvider);
    }
}
