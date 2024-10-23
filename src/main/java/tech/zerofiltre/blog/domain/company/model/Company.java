package tech.zerofiltre.blog.domain.company.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    private long id;
    private String companyName;
    private String siren;

}
