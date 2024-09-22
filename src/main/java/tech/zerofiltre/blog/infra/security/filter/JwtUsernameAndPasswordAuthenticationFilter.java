package tech.zerofiltre.blog.infra.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import tech.zerofiltre.blog.domain.user.JwtTokenProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.features.GenerateToken;
import tech.zerofiltre.blog.domain.user.features.UserNotFoundException;
import tech.zerofiltre.blog.infra.security.model.JwtAuthenticationTokenProperties;
import tech.zerofiltre.blog.infra.security.model.Token;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

public class JwtUsernameAndPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    // We use auth manager to validate the user credentials
    private final AuthenticationManager authManager;

    private final GenerateToken generateToken;

    private final Jackson2ObjectMapperBuilder objectMapperBuilder;

    private final UserProvider userProvider;

    public JwtUsernameAndPasswordAuthenticationFilter(AuthenticationManager authManager, JwtAuthenticationTokenProperties jwTokenConfiguration, VerificationTokenProvider verificationTokenProvider, JwtTokenProvider jwtTokenProvider, Jackson2ObjectMapperBuilder objectMapperBuilder, UserProvider userProvider) {
        this.authManager = authManager;
        this.objectMapperBuilder = objectMapperBuilder;
        this.userProvider = userProvider;
        this.generateToken = new GenerateToken(verificationTokenProvider, jwtTokenProvider, this.userProvider);

        // By default, UsernamePasswordAuthenticationFilter listens to "/login" path.
        // In our case, we use "/auth". So, we need to override the defaults.
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(jwTokenConfiguration.getUri(), "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

        try {
            // 1. Get credentials from request
            UserCredentials creds = null;

            creds = new ObjectMapper().readValue(request.getInputStream(), UserCredentials.class);


            // 2. Create auth object (contains credentials) which will be used by auth manager
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    creds.getUsername(), creds.getPassword(), Collections.emptyList());

            // 3. Authentication manager authenticate the user, and use DBUserDetailsService::loadUserByUsername() method to load the user.
            return authManager.authenticate(authToken);
        } catch (IOException e) {
            logger.error("An error occurred during authentication request reading", e);
            throw new RuntimeException(e);
        }


    }

    // Upon successful authentication, generate a token.
    // The 'auth' passed to successfulAuthentication() is the current authenticated user.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authenticatedUser) {

        try {
            Token token = generateToken.byEmail(authenticatedUser.getName());

            // Add token to header
            String tokenAsString = objectMapperBuilder.build().writeValueAsString(token);
            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.print(tokenAsString);
            out.flush();
        } catch (IOException | UserNotFoundException e) {
            logger.error("An error occurred during authentication", e);
            throw new RuntimeException(e);
        }
    }


    // A class just to represent the user credentials
    @Data
    private static class UserCredentials {
        private String username;
        private String password;
    }
}
