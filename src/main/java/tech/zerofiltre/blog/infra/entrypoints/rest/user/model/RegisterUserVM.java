package tech.zerofiltre.blog.infra.entrypoints.rest.user.model;

import lombok.*;
import tech.zerofiltre.blog.infra.security.config.*;

import javax.validation.constraints.*;

@Data
@ToString
@PasswordMatches
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RegisterUserVM extends PasswordHolder {
    @NotNull(message = "The full name must not be null")
    @NotEmpty(message = "The full name must not be empty")
    private String fullName;

    @NotNull(message = "The email must not be null")
    @NotEmpty(message = "The email must not be empty")
    @ValidEmail
    private String email;

    public RegisterUserVM(
            String fullName,
            String password,
            String matchingPassword,
            String email) {
        super(password, matchingPassword);
        this.fullName = fullName;
        this.email = email;
    }
}
