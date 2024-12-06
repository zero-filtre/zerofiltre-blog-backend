package tech.zerofiltre.blog.domain.user.features;

import lombok.RequiredArgsConstructor;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;

@RequiredArgsConstructor
public class IsUserExists {

    private final UserProvider userProvider;

    public boolean execute(long userId) throws ResourceNotFoundException {
        userProvider.userOfId(userId).orElseThrow(() ->
                new ResourceNotFoundException("We could not find the user", String.valueOf(userId)));

        return true;
    }

}
