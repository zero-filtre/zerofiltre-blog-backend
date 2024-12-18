package tech.zerofiltre.blog.domain.company.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkCompanyUser {

    private long companyId;
    private long userId;
    private Role role = Role.VIEWER;

    public enum Role {
        ADMIN,
        EDITOR,
        VIEWER
    }

}
