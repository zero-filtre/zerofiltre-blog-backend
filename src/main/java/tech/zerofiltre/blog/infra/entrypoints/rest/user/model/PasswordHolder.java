package tech.zerofiltre.blog.infra.entrypoints.rest.user.model;

import lombok.*;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordHolder {

    @NotNull(message = "The password must not be null")
    @NotEmpty(message = "The password must not be empty")
    private String password;
    private String matchingPassword;
}
