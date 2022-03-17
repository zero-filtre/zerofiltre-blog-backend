package tech.zerofiltre.blog.infra.security.filter;

import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.user.*;

import java.util.stream.*;


public class SocialTokenValidatorAndAuthenticator<L extends SocialLoginProvider> {

    final L socialLoginProvider;
    final UserProvider userProvider;

    public SocialTokenValidatorAndAuthenticator(L socialLoginProvider, UserProvider userProvider) {
        this.socialLoginProvider = socialLoginProvider;
        this.userProvider = userProvider;
    }

    public void validateAndAuthenticate(String token) {
        try {    // exceptions might be thrown in validating the token: if for example the token is expired

            // 4. Validate the token
            if (socialLoginProvider.isValid(token)) {
                //5. Get the user info from the token
                socialLoginProvider.userOfToken(token)
                        .ifPresent(user -> {
                            //7. Check if user in DB, otherwise update him
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
    }
}
