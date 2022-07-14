package tech.zerofiltre.blog.infra.entrypoints.rest;

import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;

@Component
public class SecurityContextManager {

    private final UserProvider userProvider;

    public SecurityContextManager(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    public User getAuthenticatedUser() throws UserNotFoundException {
        var ref = new Object() {
            String userEmail = null;
        };
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            ref.userEmail = authentication.getName();
            return userProvider.userOfEmail(ref.userEmail)
                    .orElseThrow(() -> new UserNotFoundException("No authenticated user found", ref.userEmail));
        }
        throw new UserNotFoundException("No authenticated user found", ref.userEmail);
    }
}
