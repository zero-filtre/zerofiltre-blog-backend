package tech.zerofiltre.blog.infra.providers.database.company.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class CompanyUserId implements Serializable {
    private long companyId;

    private long userId;

}
