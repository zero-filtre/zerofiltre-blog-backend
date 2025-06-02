package tech.zerofiltre.blog.infra.entrypoints.rest.company.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCompanyInfoVM {

    private long companyId;
    private String role;

}