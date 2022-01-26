package tech.zerofiltre.blog.infra.providers;

import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;

@Component
public class BasicPasswordVerifierProvider implements PasswordVerifierProvider {

    private final PasswordEncoder passwordEncoder;

    public BasicPasswordVerifierProvider(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean isValid(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }
}
