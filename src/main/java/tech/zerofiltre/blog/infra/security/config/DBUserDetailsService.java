package tech.zerofiltre.blog.infra.security.config;

import lombok.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.user.*;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class DBUserDetailsService implements UserDetailsService {

    private final UserProvider userProvider;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        tech.zerofiltre.blog.domain.user.model.User user = userProvider.userOfEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + email));
        boolean credentialsNonExpired = true;
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), user.isActive(), !user.isExpired(),
                credentialsNonExpired, !user.isLocked(), getAuthorities(user.getRoles()));
    }

    private static List<GrantedAuthority> getAuthorities(Set<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }

}
