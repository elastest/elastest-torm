package io.elastest.etm.config;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebRequestFilter implements Filter {
    final Logger logger = getLogger(lookup().lookupClass());

    @Value("${et.etm.allowed.methods}")
    public String allowedMethods;

    List<String> allowedMethodsList = Arrays.asList("*");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (allowedMethods != null && !"".equals(allowedMethods)) {
            allowedMethodsList = Arrays.asList(allowedMethods.split(","));
        }
        logger.info("Allowed Methods: {}", Arrays.asList(allowedMethodsList));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException, ElasTestViewModeException {
        HttpServletRequest req = (HttpServletRequest) request;
        String method = req.getMethod();

        if (allowedMethodsList.contains("*")
                || allowedMethodsList.contains(method)) {
            chain.doFilter(request, response);
        } else {
            final String message = "Method " + method + " not allowed";
            logger.error(message);
            throw new ElasTestViewModeException(message);
        }

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @SuppressWarnings("serial")
    public class ElasTestViewModeException extends ServletException {
        public ElasTestViewModeException(String message) {
            super(message);
        }
    }

}