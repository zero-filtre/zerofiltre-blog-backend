package tech.zerofiltre.blog.infra.providers.database.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserEmailLanguage {
    private String email;
    private String paymentEmail;
    private String language;
}
