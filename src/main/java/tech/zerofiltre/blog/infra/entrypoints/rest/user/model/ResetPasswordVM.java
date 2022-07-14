package tech.zerofiltre.blog.infra.entrypoints.rest.user.model;

import lombok.*;
import tech.zerofiltre.blog.infra.security.config.*;

import javax.validation.constraints.*;

@Data
@ToString
@PasswordMatches
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ResetPasswordVM extends PasswordHolder {

    @NotNull(message = "The token must not be null")
    @NotEmpty(message = "The token must not be empty")
    private String token;

    public ResetPasswordVM(String token, String password, String matchingPassword) {
        super(password, matchingPassword);
        this.token = token;
    }
}
