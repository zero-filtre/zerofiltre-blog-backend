package tech.zerofiltre.blog.infra.entrypoints.rest.company.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCompanyInfosVM {

    private long companyId;
    private String userRole;

}