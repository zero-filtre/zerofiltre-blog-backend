package tech.zerofiltre.blog.infra.entrypoints.rest;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.company.model.UserCompanyInfos;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.features.UserNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.UserInfoVM;
import tech.zerofiltre.blog.infra.providers.database.company.DBCompanyUserProvider;

import java.util.List;
import java.util.Optional;

@Component
public class SecurityContextManager {

    private final UserProvider userProvider;
    private final DBCompanyUserProvider dbCompanyUserProvider;


    public SecurityContextManager(UserProvider userProvider, DBCompanyUserProvider dbCompanyUserProvider) {
        this.userProvider = userProvider;
        this.dbCompanyUserProvider = dbCompanyUserProvider;
    }

    public UserInfoVM getAuthenticatedUser() throws UserNotFoundException {
        var ref = new Object() {
            String userEmail = null;
        };
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            ref.userEmail = authentication.getName();

            Optional<User> user = userProvider.userOfEmail(ref.userEmail);
            if(user.isPresent()){
                List<UserCompanyInfos> links = dbCompanyUserProvider.findAllByUserId(user.get().getId());
                UserInfoVM userInfoVM = new UserInfoVM(user.get());
                userInfoVM.setCompanies(links);
                return userInfoVM;
            }else{
                 throw  new UserNotFoundException("No authenticated user found", ref.userEmail);
            }

        }
        throw new UserNotFoundException("No authenticated user found", ref.userEmail);
    }
}
