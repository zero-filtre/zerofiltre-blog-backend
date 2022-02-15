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
import org.springframework.security.web.util.matcher.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.infra.providers.api.github.*;
import tech.zerofiltre.blog.infra.providers.api.so.*;
import tech.zerofiltre.blog.infra.security.filter.*;
import tech.zerofiltre.blog.infra.security.model.*;

@Slf4j
@Configuration
@EnableWebSecurity // Enable security config. This annotation denotes config for spring security.
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationToken jwTokenConfiguration;
    private final LoginFirstAuthenticationEntryPoint loginFirstAuthenticationEntryPoint;
    private final RoleRequiredAccessDeniedHandler roleRequiredAccessDeniedHandler;
    private final PasswordEncoder passwordEncoder;
    private final StackOverflowAuthenticationToken stackOverflowTokenConfiguration;
    private final GithubAuthenticationToken githubTokenConfiguration;
    private final StackOverflowLoginProvider stackOverflowLoginProvider;
    private final GithubLoginProvider githubLoginProvider;
    private final UserProvider userProvider;

    public SecurityConfiguration(
            UserDetailsService userDetailsService,
            JwtAuthenticationToken jwTokenConfiguration,
            LoginFirstAuthenticationEntryPoint loginFirstAuthenticationEntryPoint,
            RoleRequiredAccessDeniedHandler roleRequiredAccessDeniedHandler,
            PasswordEncoder passwordEncoder,
            StackOverflowAuthenticationToken stackOverflowTokenConfiguration,
            GithubAuthenticationToken githubTokenConfiguration, StackOverflowLoginProvider stackOverflowLoginProvider,
            GithubLoginProvider githubLoginProvider, UserProvider userProvider) {

        this.userDetailsService = userDetailsService;
        this.jwTokenConfiguration = jwTokenConfiguration;
        this.loginFirstAuthenticationEntryPoint = loginFirstAuthenticationEntryPoint;
        this.roleRequiredAccessDeniedHandler = roleRequiredAccessDeniedHandler;
        this.passwordEncoder = passwordEncoder;
        this.stackOverflowTokenConfiguration = stackOverflowTokenConfiguration;
        this.githubTokenConfiguration = githubTokenConfiguration;
        this.stackOverflowLoginProvider = stackOverflowLoginProvider;
        this.githubLoginProvider = githubLoginProvider;
        this.userProvider = userProvider;
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }


    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                //consider cors configurations
                .cors()
                .and()
                .csrf().disable()
                // make sure we use stateless session; session won't be used to store user's state.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                // handle an unauthenticated attempt
                .exceptionHandling().defaultAuthenticationEntryPointFor(loginFirstAuthenticationEntryPoint, (new AntPathRequestMatcher("/**")))
                .and()
                //TODO CUSTOMIZE BAD CREDENTIALS MESSAGES using badCredentialsAuthenticationEntryPoint or Create and ErrorHandlerController to handle /error
                // handle an authorized attempt
                .exceptionHandling().accessDeniedHandler(roleRequiredAccessDeniedHandler)
                .and()
                // Add a filter to validate user credentials and add token in the response header

                // What's the authenticationManager()?
                // An object provided by WebSecurityConfigurerAdapter, used to authenticate the user passing user's credentials
                // The filter needs this auth manager to authenticate the user.
                .addFilter(new JwtUsernameAndPasswordAuthenticationFilter(authenticationManager(), jwTokenConfiguration))
                // Add a filter to validate the tokens with every request
                .addFilterAfter(new JwtTokenAuthenticationFilter(jwTokenConfiguration), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new StackOverflowAuthenticationFilter(stackOverflowTokenConfiguration, stackOverflowLoginProvider, userProvider), JwtTokenAuthenticationFilter.class)
                .addFilterAfter(new GithubAuthenticationFilter(githubTokenConfiguration, githubLoginProvider, userProvider), StackOverflowAuthenticationFilter.class)
                .authorizeRequests()
                // allow some specific request to access without being authenticated
                .antMatchers(HttpMethod.POST,
                        "/auth",
                        jwTokenConfiguration.getUri(),
                        "/user",
                        "/user/savePasswordReset",
                        "/user/initPasswordReset",
                        "/user/github/**").permitAll()
                .antMatchers(HttpMethod.GET,
                        "/article/**",
                        "/user/registrationConfirm",
                        "/user/resendRegistrationConfirm",
                        "/user/initPasswordReset",
                        "/user/verifyTokenForPasswordReset",
                        "/tag/**",
                        "/user/profile/*",
                        "/user/github/*").permitAll()
                .antMatchers("/anonymous*").anonymous()
                .antMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                // must be an admin if trying to access admin area (authentication is also required here)
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated();

    }
}
