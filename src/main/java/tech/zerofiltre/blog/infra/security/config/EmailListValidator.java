package tech.zerofiltre.blog.infra.security.config;

import javax.validation.*;
import java.util.*;
import java.util.regex.*;

public class EmailListValidator
        implements ConstraintValidator<ValidEmailList, List<String>> {

    private static final String EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    public static boolean validateEmail(List<String> emailList) {
        if (emailList.isEmpty()) return true;
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        for (String email : emailList) {
            Matcher matcher = pattern.matcher(email).region(0, email.length());
            if (!matcher.matches()) return false;
        }
        return true;
    }

    @Override
    public void initialize(ValidEmailList constraintAnnotation) {
        // No need to implement
    }

    @Override
    public boolean isValid(List<String> emailList, ConstraintValidatorContext context) {
        return (validateEmail(emailList));
    }
}
