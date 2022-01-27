package tech.zerofiltre.blog.infra.entrypoints.rest;

import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

@Component
public class SecurityContextManager {

    private final UserProvider userProvider;

    public SecurityContextManager(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    public User getAuthenticatedUser() throws BlogException {
        String userEmail = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userEmail = authentication.getName();
        }
        return userProvider.userOfEmail(userEmail)
                .orElseThrow(() -> new BlogException("No authenticated user found"));

    }
}
