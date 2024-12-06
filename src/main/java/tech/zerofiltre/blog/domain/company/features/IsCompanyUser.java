package tech.zerofiltre.blog.domain.company.features;

import lombok.RequiredArgsConstructor;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;

import java.util.Optional;

@RequiredArgsConstructor
public class IsCompanyUser {

    private final CompanyUserProvider companyUserProvider;

    public boolean execute(long companyId, long userId) throws ResourceNotFoundException {
        Optional<LinkCompanyUser> companyUser = companyUserProvider.findByCompanyIdAndUserId(companyId, userId);

        if(companyUser.isEmpty()) {
            throw new ResourceNotFoundException("We could not find the company user", "");
        }
        return true;
    }

}
