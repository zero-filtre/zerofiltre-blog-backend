package tech.zerofiltre.blog.infra.providers.database.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEmailLanguage {
    private String email;
    private String paymentEmail;
    private String language;
}
