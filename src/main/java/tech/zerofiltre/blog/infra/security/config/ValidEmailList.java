package tech.zerofiltre.blog.infra.security.config;

import javax.validation.*;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({TYPE, FIELD, ANNOTATION_TYPE,PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = EmailListValidator.class)
@Documented
public @interface ValidEmailList {
    String message() default "At least one invalid email";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
