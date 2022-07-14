package tech.zerofiltre.blog.infra.entrypoints.rest.user.model;

import lombok.*;
import tech.zerofiltre.blog.infra.security.config.*;

import javax.validation.constraints.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@PasswordMatches
public class PasswordHolder {

    @NotNull(message = "The password must not be null")
    @NotEmpty(message = "The password must not be empty")
    @ValidPassword
    private String password;
    private String matchingPassword;
}
