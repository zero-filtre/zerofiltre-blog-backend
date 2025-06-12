package tech.zerofiltre.blog.infra.entrypoints.rest.company.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCompanyInfoVM implements Serializable {

    private static final long serialVersionUID = 1L;

    private long companyId;
    private String role;

}