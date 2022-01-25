package tech.zerofiltre.blog.infra.entrypoints.rest.user.model;

import lombok.*;
import tech.zerofiltre.blog.infra.security.config.*;

import javax.validation.constraints.*;

@Data
@PasswordMatches
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RegisterUserVM extends PasswordHolder {
    @NotNull(message = "The firstName must not be null")
    @NotEmpty(message = "The firstName must not be empty")
    private String firstName;

    @NotNull(message = "The lastName must not be null")
    @NotEmpty(message = "The lastName must not be empty")
    private String lastName;

    @NotNull(message = "The email must not be null")
    @NotEmpty(message = "The email must not be empty")
    @ValidEmail
    private String email;

    public RegisterUserVM(
            String firstName,
            String lastName,
            String password,
            String matchingPassword,
            String email) {
        super(password, matchingPassword);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
}
