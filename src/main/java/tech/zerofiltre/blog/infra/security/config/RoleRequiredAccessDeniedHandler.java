package tech.zerofiltre.blog.infra.security.config;

import com.fasterxml.jackson.databind.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.http.*;
import org.springframework.security.access.*;
import org.springframework.security.web.access.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.error.*;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import javax.servlet.http.*;
import java.io.*;

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
                ZerofiltreUtils.getRootCauseMessage(accessDeniedException)
        );

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), error);
    }
}
