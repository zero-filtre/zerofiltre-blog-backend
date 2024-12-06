package tech.zerofiltre.blog.domain.company.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkCompanyUser {

    private long companyId;
    private long userId;
    private Role role = Role.VIEWER;

    @Getter
    @AllArgsConstructor
    public enum Role {
        ADMIN("ROLE_COMPANY_ADMIN"),
        EDITOR("ROLE_COMPANY_EDITOR"),
        VIEWER("ROLE_COMPANY_VIEWER");

        private final String value;
    }

}
