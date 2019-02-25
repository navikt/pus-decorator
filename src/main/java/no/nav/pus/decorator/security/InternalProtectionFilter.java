package no.nav.pus.decorator.security;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Slf4j
public class InternalProtectionFilter implements Filter {

    private static final Pattern SUBNET_IP_PATTERN = Pattern.compile("192\\.168\\.\\d{1,3}\\.\\d{1,3}");
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
        if (isAllowedAccessToInternal(hostname)) {
            chain.doFilter(request, response);
        } else {
            log.warn("blocking access to unsafe host: {}", hostname);
            httpServletResponse.setStatus(SC_FORBIDDEN);
            httpServletResponse.setContentType("text/plain");
            httpServletResponse.getWriter().write("FORBIDDEN");
        }
    }

    static boolean isAllowedAccessToInternal(String hostname) {
        return "localhost".equals(hostname) // allow for local development and testing
                || SUBNET_IP_PATTERN.matcher(hostname).matches() // kubernetes uses subnet ips to access internal endpoints
                || SAFE_POSTFIXES.stream().anyMatch(hostname::endsWith) // allow access from internal addresses
                ;
    }

    @Override
    public void destroy() {
    }

}