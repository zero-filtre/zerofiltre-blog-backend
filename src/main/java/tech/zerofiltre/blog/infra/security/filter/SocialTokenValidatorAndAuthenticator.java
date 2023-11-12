package tech.zerofiltre.blog.infra.security.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import tech.zerofiltre.blog.domain.Domains;
import tech.zerofiltre.blog.domain.error.UnAuthenticatedActionException;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.metrics.model.CounterSpecs;
import tech.zerofiltre.blog.domain.user.SocialLoginProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.use_cases.UserNotFoundException;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class SocialTokenValidatorAndAuthenticator<L extends SocialLoginProvider> {

    public static final String SUCCESS = "success";
    final L socialLoginProvider;
    final UserProvider userProvider;
    private final MetricsProvider metricsProvider;
    private final SecurityContextManager securityContextManager;

    public SocialTokenValidatorAndAuthenticator(L socialLoginProvider, UserProvider userProvider, MetricsProvider metricsProvider, SecurityContextManager securityContextManager) {
        this.socialLoginProvider = socialLoginProvider;
        this.userProvider = userProvider;
        this.metricsProvider = metricsProvider;
        this.securityContextManager = securityContextManager;
    }

    public void validateAndAuthenticate(String token) {
        try {    // exceptions might be thrown in validating the token: if for example the token is expired

            // 4. Validate the token
            if (socialLoginProvider.isValid(token)) {
                //5. Get the user info from the token
                Optional<User> userOfToken = socialLoginProvider.userOfToken(token);
                if (userOfToken.isPresent()) {
                    User user = userOfToken.get();
                    //7. Check if user in DB, otherwise save him
                    Optional<User> foundUser = userProvider.userOfSocialId(user.getSocialId());
                    if (foundUser.isEmpty()) {
                        user = save(user);
                    } else {
                        user = foundUser.get();
                        recordConnectionMetrics(user.getLoginFrom().toString(), user);
                    }
                    // 8. Create auth object
                    // UsernamePasswordAuthenticationToken: A built-in object, used by spring to represent the current authenticated / being authenticated user.
                    // It needs a list of authorities, which has type of GrantedAuthority interface, where SimpleGrantedAuthority is an implementation of that interface
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            user.getEmail(), null, user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

                    // 9. Authenticate the user
                    // Now, user is authenticated
                    SecurityContextHolder.getContext().setAuthentication(auth);

                }
            }

        } catch (Exception e) {
            log.error("Error when checking token ", e);
            // In case of failure. Make sure it's clear; so guarantee user won't be authenticated
            SecurityContextHolder.clearContext();
        }
    }

    private User save(User user) throws UnAuthenticatedActionException {
        try {
            user = userProvider.save(user);
            recordAccountCreationMetrics(user);
        } catch (DataIntegrityViolationException e) {
            log.info("User already exists, update him with socialId", e);
            Optional<User> existingUser = userProvider.userOfEmail(user.getSocialId()); //stackoverflow
            if (existingUser.isEmpty()) existingUser = userProvider.userOfEmail(user.getEmail()); //github
            if (existingUser.isPresent()) {
                existingUser.get().setSocialId(user.getSocialId());
                user = userProvider.save(existingUser.get());
                recordConnectionMetrics(user.getLoginFrom().toString(), existingUser.get());
            } else {
                log.error("User already exists but we do not find it in DB");
                throw e;
            }
        }
        return user;
    }

    private void recordAccountCreationMetrics(User user) {
        CounterSpecs counterSpecs = new CounterSpecs();
        counterSpecs.setName(CounterSpecs.ZEROFILTRE_ACCOUNT_CREATIONS);
        counterSpecs.setTags("from", user.getLoginFrom().toString(), SUCCESS, "true");
        metricsProvider.incrementCounter(counterSpecs);
    }

    private void recordConnectionMetrics(String loginFrom, User foundUser) throws UnAuthenticatedActionException {
        CounterSpecs counterSpecs = new CounterSpecs();
        try {
            securityContextManager.getAuthenticatedUser();
        } catch (UserNotFoundException une) {
            counterSpecs.setName(CounterSpecs.ZEROFILTRE_ACCOUNT_CONNECTIONS);
            counterSpecs.setTags("from", loginFrom, SUCCESS, "true");
            metricsProvider.incrementCounter(counterSpecs);
        }

        if (foundUser.isExpired()) {

            counterSpecs.setName(CounterSpecs.ZEROFILTRE_ACCOUNT_CONNECTIONS);
            counterSpecs.setTags("from", loginFrom, SUCCESS, "false");
            metricsProvider.incrementCounter(counterSpecs);

            throw new UnAuthenticatedActionException(
                    String.format("The user %s has been deactivated, not allowing connection until activation", foundUser.getFullName()),
                    Domains.NONE.name());
        }
    }
}
