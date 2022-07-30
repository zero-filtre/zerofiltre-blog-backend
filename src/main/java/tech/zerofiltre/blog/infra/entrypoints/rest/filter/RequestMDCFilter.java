package tech.zerofiltre.blog.infra.entrypoints.rest.filter;

import lombok.extern.slf4j.*;
import org.slf4j.*;
import org.springframework.core.annotation.*;
import org.springframework.stereotype.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

@Slf4j
@Component
public class RequestMDCFilter implements Filter {

    private static final String X_REQUEST_ID = "X-Request-ID";

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        try {
            addXRequestId(req);
            log.info("path: {}, method: {}, query {}", req.getRequestURI(), req.getMethod(), req.getQueryString());
            res.setHeader(X_REQUEST_ID, MDC.get(X_REQUEST_ID));
            chain.doFilter(request, response);
        } finally {
            log.info("statusCode {}, path: {}, method: {}, query {}", res.getStatus(), req.getRequestURI(), req.getMethod(), req.getQueryString());
            MDC.clear();
        }
    }

    private void addXRequestId(HttpServletRequest request) {
        String xRequestId = request.getHeader(X_REQUEST_ID);
        if (xRequestId == null) {
            MDC.put(X_REQUEST_ID, UUID.randomUUID().toString());
        } else {
            MDC.put(X_REQUEST_ID, xRequestId);
        }
    }

}
