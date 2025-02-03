package tech.zerofiltre.blog.domain.company.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkCompanyUser {

    private long id;
    private long companyId;
    private long userId;
    private Role role = Role.VIEWER;
    private boolean active = true;
    private LocalDateTime linkedAt;
    private LocalDateTime suspendedAt;

    public enum Role {
        ADMIN,
        EDITOR,
        VIEWER
    }

}
