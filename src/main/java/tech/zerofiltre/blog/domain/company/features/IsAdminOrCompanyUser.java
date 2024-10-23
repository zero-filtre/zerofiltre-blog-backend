package tech.zerofiltre.blog.domain.company.features;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.company.CompanyUserProvider;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IsAdminOrCompanyUser {

    private final CompanyUserProvider companyUserProvider;

    public boolean execute(User connectedUser, long companyId) throws ForbiddenActionException {
        if(!connectedUser.isAdmin()) {
            Optional<LinkCompanyUser> companyUser = companyUserProvider.findByCompanyIdAndUserId(companyId, connectedUser.getId());

            if(companyUser.isEmpty()) {
                throw new ForbiddenActionException("You don't have authorization.");
            }
        }
        return true;
    }

}
