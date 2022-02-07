package tech.zerofiltre.blog.infra.security.filter;

import com.fasterxml.jackson.databind.*;
import lombok.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.util.matcher.*;
import tech.zerofiltre.blog.infra.security.model.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;

public class JwtUsernameAndPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    // We use auth manager to validate the user credentials
    private final AuthenticationManager authManager;

    private final JwtAuthenticationToken jwTokenConfiguration;

    public JwtUsernameAndPasswordAuthenticationFilter(AuthenticationManager authManager, JwtAuthenticationToken jwTokenConfiguration) {
        this.authManager = authManager;
        this.jwTokenConfiguration = jwTokenConfiguration;

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
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authenticatedUser) {

        String token = jwTokenConfiguration.buildToken(
                authenticatedUser.getName(),
                authenticatedUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet())
        );
        // Add token to header
        response.addHeader(jwTokenConfiguration.getHeader(), jwTokenConfiguration.getPrefix() + " " + token);
    }


    // A class just to represent the user credentials
    @Data
    private static class UserCredentials {
        private String username;
        private String password;
    }
}
