package tech.zerofiltre.blog.infra.security.config;

import lombok.extern.slf4j.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.config.annotation.authentication.builders.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.http.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.*;
import org.springframework.security.web.authentication.*;
import tech.zerofiltre.blog.infra.security.filter.*;

@Slf4j
@Configuration
@EnableWebSecurity // Enable security config. This annotation denotes config for spring security.
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final JwtConfiguration jwtConfiguration;
    private final LoginFirstAuthenticationEntryPoint loginFirstAuthenticationEntryPoint;
    private final RoleRequiredAccessDeniedHandler roleRequiredAccessDeniedHandler;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfiguration(UserDetailsService userDetailsService, JwtConfiguration jwtConfiguration, LoginFirstAuthenticationEntryPoint loginFirstAuthenticationEntryPoint, RoleRequiredAccessDeniedHandler roleRequiredAccessDeniedHandler, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.jwtConfiguration = jwtConfiguration;
        this.loginFirstAuthenticationEntryPoint = loginFirstAuthenticationEntryPoint;
        this.roleRequiredAccessDeniedHandler = roleRequiredAccessDeniedHandler;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }


    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                // make sure we use stateless session; session won't be used to store user's state.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // handle an unauthenticated attempt
                .exceptionHandling().authenticationEntryPoint(loginFirstAuthenticationEntryPoint)
                .and()
                // handle an authorized attempt
                .exceptionHandling().accessDeniedHandler(roleRequiredAccessDeniedHandler)
                .and()
                // Add a filter to validate user credentials and add token in the response header

                // What's the authenticationManager()?
                // An object provided by WebSecurityConfigurerAdapter, used to authenticate the user passing user's credentials
                // The filter needs this auth manager to authenticate the user.
                .addFilter(new JwtUsernameAndPasswordAuthenticationFilter(authenticationManager(), jwtConfiguration))
                // Add a filter to validate the tokens with every request
                .addFilterAfter(new JwtTokenAuthenticationFilter(jwtConfiguration), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                // allow all who are accessing "auth" service or trying to register an account
                .antMatchers(HttpMethod.POST, jwtConfiguration.getUri()).permitAll()
                .antMatchers(HttpMethod.POST, "/user").permitAll()
                .antMatchers(HttpMethod.POST, "/auth").permitAll()
                .antMatchers("/article/**").permitAll()
                .antMatchers(HttpMethod.GET, "/user/registrationConfirm", "/user/resendRegistrationConfirm").permitAll()
                // must be an admin if trying to access admin area (authentication is also required here)
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/anonymous*").anonymous()
                .antMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated();

    }
}
