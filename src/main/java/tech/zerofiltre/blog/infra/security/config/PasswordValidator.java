package tech.zerofiltre.blog.infra.security.config;

import javax.validation.*;
import java.util.regex.*;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final String PASSWORD_PATTERN = "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{6,}";

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // No need to implement
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return (validatePassword(password));
    }

    private boolean validatePassword(String password) {
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}