package tech.zerofiltre.blog.infra.entrypoints.rest.user.model;

import lombok.*;
import tech.zerofiltre.blog.infra.security.config.*;

import javax.validation.constraints.*;

@Data
@PasswordMatches
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserVM {
    @NotNull(message = "The firstName must not be null")
    @NotEmpty(message = "The firstName must not be empty")
    private String firstName;

    @NotNull(message = "The lastName must not be null")
    @NotEmpty(message = "The lastName must not be empty")
    private String lastName;

    @NotNull(message = "The password must not be null")
    @NotEmpty(message = "The password must not be empty")
    private String password;
    private String matchingPassword;

    @NotNull(message = "The email must not be null")
    @NotEmpty(message = "The email must not be empty")
    @ValidEmail
    private String email;

}
