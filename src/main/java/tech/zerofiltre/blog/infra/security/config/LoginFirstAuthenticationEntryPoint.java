package tech.zerofiltre.blog.infra.security.config;

import com.fasterxml.jackson.databind.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.http.*;
import org.springframework.security.core.*;
import org.springframework.security.web.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.error.*;

import javax.servlet.http.*;
import java.io.*;

@Slf4j
@Component
public class LoginFirstAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Value("${zerofiltre.infra.entrypoints.rest.api-version}")
    private String currentApiVersion;

    @Autowired
    private MessageSource messageSource;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        log.error("Authentication Error", authException);

        String code = "ZBLOG_007";
        BlogError error = new BlogError(
                currentApiVersion,
                String.valueOf(HttpServletResponse.SC_UNAUTHORIZED),
                code,
                messageSource.getMessage(code, null, request.getLocale()),
                "no domain available",
                authException.getLocalizedMessage()
        );

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), error);
    }


}
