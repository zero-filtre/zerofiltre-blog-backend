package tech.zerofiltre.blog.domain.company.features;

import lombok.RequiredArgsConstructor;
import tech.zerofiltre.blog.domain.company.model.LinkCompanyUser;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.user.model.User;

@RequiredArgsConstructor
public class HasPermission {

    public boolean execute(User connectedUser, boolean isCompanyAdmin, LinkCompanyUser.Role role) throws ForbiddenActionException {
        if (role.equals(LinkCompanyUser.Role.ADMIN)
                && !connectedUser.isAdmin()) {
            throw new ForbiddenActionException("You don't have authorization.");
        } else if ((role.equals(LinkCompanyUser.Role.EDITOR)
                || role.equals(LinkCompanyUser.Role.VIEWER))
                && !connectedUser.isAdmin()
                && !isCompanyAdmin) {
            throw new ForbiddenActionException("You don't have authorization.");
        }
        return true;
    }

}
/*
// solution chatgpt
    void checkIfBadUserOrBadCompanyRoleOrBadRole(User connectedUser, long companyId, CompanyUser.Role role) throws ForbiddenActionException {
        boolean isEditorOrViewer = role.equals(CompanyUser.Role.EDITOR) || role.equals(CompanyUser.Role.VIEWER);
        boolean isAdmin = role.equals(CompanyUser.Role.ADMIN);

        boolean hasPermission =
        (isEditorOrViewer && !checkIfUserIsAdminOrCompanyAdmin.execute(connectedUser, companyId)) ||
        (isAdmin && !connectedUser.isAdmin());

        if (hasPermission) {
            throw new ForbiddenActionException("You don't have authorization.");
        }
    }
*/
