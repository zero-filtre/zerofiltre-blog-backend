package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

@Component
@RequiredArgsConstructor
public class VerificationTokenManager {

    private final VerificationTokenProvider verificationTokenProvider;

    public String generateToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = verificationTokenProvider.ofUser(user)
                .map(vToken -> {
                    vToken.setToken(token);
                    vToken.setExpiryDate(LocalDateTime.now().plusDays(1));
                    return vToken;
                }).orElse(new VerificationToken(user, token));
        return verificationTokenProvider.save(verificationToken).getToken();
    }
}
