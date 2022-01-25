package tech.zerofiltre.blog.infra.entrypoints.rest.user.model;

import lombok.*;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpdatePasswordVM extends PasswordHolder {

    @NotNull(message = "The password must not be null")
    @NotEmpty(message = "The password must not be empty")
    private String oldPassword;
}
