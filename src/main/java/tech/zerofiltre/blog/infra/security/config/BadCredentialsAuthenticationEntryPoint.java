package tech.zerofiltre.blog.infra.security.config;

import com.fasterxml.jackson.databind.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.http.*;
import org.springframework.security.core.*;
import org.springframework.security.web.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.error.*;

import javax.servlet.http.*;
import java.io.*;

@Component
public class BadCredentialsAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Value("${zerofiltre.infra.entrypoints.rest.api-version}")
    private String currentApiVersion;

    @Autowired
    private MessageSource messageSource;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        BlogError error = new BlogError(
                currentApiVersion,
                String.valueOf(HttpServletResponse.SC_UNAUTHORIZED),
                "ZBLOG_009",
                messageSource.getMessage("ZBLOG_009", null, request.getLocale()),
                authException.getLocalizedMessage()
        );

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), error);
    }
}
