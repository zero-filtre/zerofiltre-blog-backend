package tech.zerofiltre.blog.infra.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.infra.entrypoints.rest.error.BlogError;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

@Component
public class RoleRequiredAccessDeniedHandler implements AccessDeniedHandler {

    @Value("${zerofiltre.infra.entrypoints.rest.api-version}")
    private String currentApiVersion;

    @Autowired
    private MessageSource messageSource;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        BlogError error = new BlogError(
                currentApiVersion,
                String.valueOf(HttpServletResponse.SC_FORBIDDEN),
                "ZBLOG_008",
                messageSource.getMessage("ZBLOG_008", null, request.getLocale()),
                getRootCauseMessage(accessDeniedException)
        );

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), error);
    }
}
