package tech.zerofiltre.blog.domain.user;

import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.infra.providers.database.user.model.*;

import java.util.*;

public interface VerificationTokenProvider {

    Optional<VerificationToken> ofToken(String token);

    Optional<VerificationToken> ofUser(User user);

    VerificationToken save(VerificationToken verificationToken);
}
