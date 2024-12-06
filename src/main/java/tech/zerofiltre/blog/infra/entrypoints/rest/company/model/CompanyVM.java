package tech.zerofiltre.blog.infra.entrypoints.rest.company.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyVM {

    @Min(value = 1, message = "The id must be greater or equal than one")
    private long id;

    @NotBlank(message = "The company name must not be blank")
    private String companyName;

    @Size(min=9, max=9, message = "The SIREN number must have 9 digits")
    private String siren;

}
