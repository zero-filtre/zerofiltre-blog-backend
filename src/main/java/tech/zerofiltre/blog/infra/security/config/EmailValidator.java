package tech.zerofiltre.blog.infra.security.config;

import javax.validation.*;
import java.util.regex.*;

public class EmailValidator
        implements ConstraintValidator<ValidEmail, String> {

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*\\.[A-Za-z]{2,}$";


    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        // No need to implement
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return (validateEmail(email));
    }

    private boolean validateEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
