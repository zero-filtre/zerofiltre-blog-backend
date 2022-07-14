package tech.zerofiltre.blog.infra.security.config;

import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.*;

import javax.validation.*;

public class PasswordMatchesValidator
        implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        // No need to implement
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        PasswordHolder holder = (PasswordHolder) obj;
        return holder.getPassword().equals(holder.getMatchingPassword());
    }
}
