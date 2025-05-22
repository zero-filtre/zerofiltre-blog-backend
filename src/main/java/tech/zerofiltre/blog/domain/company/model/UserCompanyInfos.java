package tech.zerofiltre.blog.domain.company.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCompanyInfos {
    private long companyId;
    private LinkCompanyUser.Role userRole;
}
