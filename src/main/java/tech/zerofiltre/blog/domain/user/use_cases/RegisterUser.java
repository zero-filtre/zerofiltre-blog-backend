package tech.zerofiltre.blog.domain.user.use_cases;

import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.metrics.*;
import tech.zerofiltre.blog.domain.metrics.model.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

public class RegisterUser {
    private final UserProvider userProvider;
    private final AvatarProvider avatarProvider;
    private final MetricsProvider metricsProvider;

    public RegisterUser(UserProvider userProvider, AvatarProvider avatarProvider, MetricsProvider metricsProvider) {
        this.userProvider = userProvider;
        this.avatarProvider = avatarProvider;
        this.metricsProvider = metricsProvider;
    }

    public User execute(User user) throws ResourceAlreadyExistException {
        CounterSpecs counterSpecs = new CounterSpecs();
        counterSpecs.setName(CounterSpecs.ZEROFILTRE_ACCOUNT_CREATIONS);

        if (userProvider.userOfEmail(user.getEmail()).isPresent()) {
            counterSpecs.setTags("from", "email", "success", "false");
            metricsProvider.incrementCounter(counterSpecs);
            throw new ResourceAlreadyExistException("There is an existing account with that email address: " + user.getEmail(), user.getEmail(), Domains.USER.name());
        }

        counterSpecs.setTags("from", "email", "success", "true");
        metricsProvider.incrementCounter(counterSpecs);

        user.setProfilePicture(avatarProvider.byEmail(user.getEmail()));
        return userProvider.save(user);
    }
}
