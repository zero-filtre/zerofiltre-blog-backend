package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.Domains;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;

public class ResetPasswordNotAllowedException extends ResourceNotFoundException {
    public ResetPasswordNotAllowedException(String message, String resourceId) {
        super(message, resourceId, Domains.USER.name());
    }
}
