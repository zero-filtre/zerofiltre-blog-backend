package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.error.*;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(String message, String resourceId) {
        super(message, resourceId, Domains.USER.name());
    }
}
