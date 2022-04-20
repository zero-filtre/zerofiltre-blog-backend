package tech.zerofiltre.blog.infra.security.filter;

import lombok.extern.slf4j.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.context.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.util.*;
import java.util.stream.*;

@Slf4j
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
                Optional<User> userOfToken = socialLoginProvider.userOfToken(token);
                if (userOfToken.isPresent()) {
                    User user = userOfToken.get();
                    //7. Check if user in DB, otherwise save him
                    Optional<User> foundUser = userProvider.userOfEmail(user.getEmail());
                    if (foundUser.isEmpty()) {
                        userProvider.save(user);
                    } else {
                        if (foundUser.get().isExpired())
                            throw new UnAuthenticatedActionException(
                                    String.format("The user %s has been deactivated, not allowing connection until activation", user.getFullName()),
                                    Domains.NONE.name());
                    }
                    // 8. Create auth object
                    // UsernamePasswordAuthenticationToken: A built-in object, used by spring to represent the current authenticated / being authenticated user.
                    // It needs a list of authorities, which has type of GrantedAuthority interface, where SimpleGrantedAuthority is an implementation of that interface
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            user.getEmail(), null, user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

                    // 9. Authenticate the user
                    // Now, user is authenticated
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

        } catch (Exception e) {
            log.error("Error when checking token ", e);
            // In case of failure. Make sure it's clear; so guarantee user won't be authenticated
            SecurityContextHolder.clearContext();
        }
    }
}
