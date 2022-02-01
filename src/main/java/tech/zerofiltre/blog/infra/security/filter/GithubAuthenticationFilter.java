package tech.zerofiltre.blog.infra.security.filter;

import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.context.*;
import org.springframework.web.filter.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.infra.providers.api.github.*;
import tech.zerofiltre.blog.infra.security.config.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.stream.*;

public class GithubAuthenticationFilter extends OncePerRequestFilter {

    private final GithubTokenConfiguration githubTokenConfiguration;
    private final GithubLoginProvider socialLoginProvider;
    private final UserProvider userProvider;

    public GithubAuthenticationFilter(GithubTokenConfiguration githubTokenConfiguration, GithubLoginProvider socialLoginProvider, UserProvider userProvider) {
        this.githubTokenConfiguration = githubTokenConfiguration;
        this.socialLoginProvider = socialLoginProvider;
        this.userProvider = userProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        // 1. get the authentication header. Tokens are supposed to be passed in the authentication header
        String header = request.getHeader(githubTokenConfiguration.getHeader());

        // 2. validate the header and check the prefix
        if (header == null || !header.startsWith(githubTokenConfiguration.getPrefix())) {
            chain.doFilter(request, response);        // If not valid, go to the next filter.
            return;
        }

        // If there is no token provided and hence the user won't be authenticated.
        // It's Ok. Maybe the user is accessing a public path or asking for a token.

        // All secured paths that needs a token are already defined and secured in config class.
        // And If user tried to access without access token, then he won't be authenticated and an exception will be thrown.

        // 3. Get the token
        String token = header.replace(githubTokenConfiguration.getPrefix(), "");

        try {    // exceptions might be thrown in validating the token: if for example the token is expired

            // 4. Validate the token
            if (socialLoginProvider.isValid(token)) {
                //5. Get the user info from the token
                socialLoginProvider.userOfToken(token)
                        .ifPresent(user -> {
                            //7. Check if user in DB, otherwise save him
                            userProvider.userOfEmail(user.getEmail())
                                    .ifPresentOrElse(user1 -> {
                                    }, () -> userProvider.save(user));

                            // 8. Create auth object
                            // UsernamePasswordAuthenticationToken: A built-in object, used by spring to represent the current authenticated / being authenticated user.
                            // It needs a list of authorities, which has type of GrantedAuthority interface, where SimpleGrantedAuthority is an implementation of that interface
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                    user.getEmail(), null, user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

                            // 9. Authenticate the user
                            // Now, user is authenticated
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
            }
        } catch (Exception e) {
            // In case of failure. Make sure it's clear; so guarantee user won't be authenticated
            SecurityContextHolder.clearContext();
        }
        // go to the next filter in the filter chain
        chain.doFilter(request, response);
    }
}
