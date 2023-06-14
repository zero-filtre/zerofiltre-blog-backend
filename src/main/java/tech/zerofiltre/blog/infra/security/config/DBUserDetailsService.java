package tech.zerofiltre.blog.infra.security.config;

import lombok.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.metrics.*;
import tech.zerofiltre.blog.domain.metrics.model.*;
import tech.zerofiltre.blog.domain.user.*;

import java.util.stream.*;

@Service
@Transactional
@RequiredArgsConstructor
public class DBUserDetailsService implements UserDetailsService {

    private final UserProvider userProvider;
    private final MetricsProvider metricsProvider;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        CounterSpecs counterSpecs = new CounterSpecs();
        counterSpecs.setName(CounterSpecs.ZEROFILTRE_ACCOUNT_CONNECTIONS);

        tech.zerofiltre.blog.domain.user.model.User user = userProvider.userOfEmail(email)
                .orElseThrow(() -> {
                    counterSpecs.setTags("from", "email", "success", "false");
                    metricsProvider.incrementCounter(counterSpecs);
                    return new UsernameNotFoundException("No user found with username: " + email);
                });

        counterSpecs.setTags("from", "email", "success", "true");
        metricsProvider.incrementCounter(counterSpecs);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), true, !user.isExpired(),
                true, !user.isLocked(),
                user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
    }

}
