package no.nav.pus.decorator.security;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static no.nav.apiapp.ApiAppServletContextListener.INTERNAL_IS_ALIVE;

@Slf4j
public class InternalProtectionFilter implements Filter {

    private static final List<Pattern> SUBNET_IP_PATTERNS = Arrays.asList(
            Pattern.compile("192\\.168\\.\\d{1,3}\\.\\d{1,3}") // on-prem
    );

    private static final List<String> SAFE_POSTFIXES = new ArrayList<>(asList(
            ".oera.no",
            ".oera-q.local",
            ".adeo.no",
            ".preprod.local"
    ));

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String hostname = httpServletRequest.getServerName();
        String servletPath = httpServletRequest.getServletPath();
        if (isAllowedAccessToInternal(hostname) || isPublicPath(servletPath)) {
            chain.doFilter(request, response);
        } else {
            log.warn("blocking access to unsafe host: {}", hostname);
            httpServletResponse.setStatus(SC_FORBIDDEN);
            httpServletResponse.setContentType("text/plain");
            httpServletResponse.getWriter().write("FORBIDDEN");
        }
    }

    static boolean isPublicPath(String servletPath) {
        return INTERNAL_IS_ALIVE.equals(servletPath);
    }

    static boolean isAllowedAccessToInternal(String hostname) {
        return "localhost".equals(hostname) // allow for local development and testing
                || SUBNET_IP_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(hostname).matches()) // kubernetes uses subnet ips to access internal endpoints
                || SAFE_POSTFIXES.stream().anyMatch(hostname::endsWith) // allow access from internal addresses
                ;


    }

    @Override
    public void destroy() {
    }

}
