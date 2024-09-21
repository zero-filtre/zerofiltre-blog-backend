package tech.zerofiltre.blog.infra.entrypoints.rest;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.features.UserNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;

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
