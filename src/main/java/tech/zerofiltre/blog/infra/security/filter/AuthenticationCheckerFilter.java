package tech.zerofiltre.blog.infra.security.filter;

import org.springframework.web.filter.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.infra.security.model.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class AuthenticationCheckerFilter<T extends AuthenticationTokenProperties, L extends SocialLoginProvider> extends OncePerRequestFilter {

    final T tokenConfiguration;
    final SocialTokenValidatorAndAuthenticator<L> socialTokenValidatorAndAuthenticator;


    public AuthenticationCheckerFilter(T tokenConfiguration, SocialTokenValidatorAndAuthenticator<L> socialTokenValidatorAndAuthenticator) {
        this.tokenConfiguration = tokenConfiguration;
        this.socialTokenValidatorAndAuthenticator = socialTokenValidatorAndAuthenticator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        // 1. get the authentication header. Tokens are supposed to be passed in the authentication header
        String header = request.getHeader(tokenConfiguration.getHeader());

        // 2. validate the header and check the prefix
        if (header == null || !header.startsWith(tokenConfiguration.getPrefix())) {
            chain.doFilter(request, response);        // If not valid, go to the next filter.
            return;
        }

        // If there is no token provided and hence the user won't be authenticated.
        // It's Ok. Maybe the user is accessing a public path or asking for a token.

        // All secured paths that needs a token are already defined and secured in config class.
        // And If user tried to access without access token, then he won't be authenticated and an exception will be thrown.

        // 3. Get the token
        String token = header.replace(tokenConfiguration.getPrefix(), "");
        socialTokenValidatorAndAuthenticator.validateAndAuthenticate(token);

        // go to the next filter in the filter chain
        chain.doFilter(request, response);
    }
}
