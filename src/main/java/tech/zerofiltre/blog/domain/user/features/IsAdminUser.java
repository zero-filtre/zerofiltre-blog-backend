package tech.zerofiltre.blog.domain.user.features;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.user.model.User;

@Component
@NoArgsConstructor
public class IsAdminUser {

    public boolean execute(User user) throws ForbiddenActionException {
        if(!user.isAdmin())
            throw new ForbiddenActionException("The user must be a Zerofiltre administrator.");

        return true;
    }

}
