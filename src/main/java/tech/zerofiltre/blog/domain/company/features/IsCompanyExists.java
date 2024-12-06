package tech.zerofiltre.blog.domain.company.features;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.company.CompanyProvider;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;

@Component
@RequiredArgsConstructor
public class IsCompanyExists {

    private final CompanyProvider companyProvider;

    public boolean execute(long companyId) throws ResourceNotFoundException {
        companyProvider.findById(companyId).orElseThrow(() ->
                new ResourceNotFoundException("We could not find the company", String.valueOf(companyId)));

        return true;
    }

}
