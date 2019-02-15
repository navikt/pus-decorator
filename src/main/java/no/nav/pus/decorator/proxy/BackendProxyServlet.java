package no.nav.pus.decorator.proxy;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.ObjectAppendingMarker;
import no.nav.apiapp.security.SecurityLevelAuthorizationModule;
import no.nav.apiapp.selftest.Helsesjekk;
import no.nav.apiapp.selftest.HelsesjekkMetadata;
import no.nav.common.auth.Subject;
import no.nav.log.LogFilter;
import no.nav.log.MDCConstants;
import no.nav.pus.decorator.login.LoginService;
import no.nav.sbl.util.EnvironmentUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.proxy.ProxyServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static java.util.Optional.ofNullable;
import static no.nav.log.LogFilter.CONSUMER_ID_HEADER_NAME;
import static no.nav.log.LogFilter.PREFERRED_NAV_CALL_ID_HEADER_NAME;
import static no.nav.pus.decorator.proxy.BackendProxyConfig.RequestRewrite.REMOVE_CONTEXT_PATH;
import static no.nav.sbl.util.StringUtils.*;

@Slf4j
public class BackendProxyServlet extends ProxyServlet implements Helsesjekk {

    private static final String CALL_ID = "callId";

    private final String applicationName = EnvironmentUtils.requireApplicationName();

    private final BackendProxyConfig backendProxyConfig;
    private final String id;
    private final HelsesjekkMetadata helsesjekkMetadata;
    private final String pingUrl;
    private final boolean removeContextPath;
    private final int contextPathLength;
    private final LoginService loginService;
    private final SecurityLevelAuthorizationModule securityLevelAuthorizationModule;

    public BackendProxyServlet(BackendProxyConfig backendProxyConfig, LoginService loginService) {
        this.backendProxyConfig = backendProxyConfig;
        this.id = (BackendProxyServlet.class.getSimpleName() + "_" + backendProxyConfig.contextPath.substring(1)).toLowerCase();
        this.removeContextPath = backendProxyConfig.requestRewrite == REMOVE_CONTEXT_PATH;
        this.contextPathLength = backendProxyConfig.contextPath.length();

        this.pingUrl = backendProxyConfig.baseUrl + ofNullable(backendProxyConfig.pingRequestPath).orElseGet(this::defaultPingPath);
        this.helsesjekkMetadata = new HelsesjekkMetadata(
                "proxy_" + id,
                pingUrl,
                "ping backend for " + backendProxyConfig.contextPath,
                false
        );
        this.loginService = loginService;
        this.securityLevelAuthorizationModule =
                backendProxyConfig.minSecurityLevel != null && backendProxyConfig.minSecurityLevel > 0
                        ? new SecurityLevelAuthorizationModule(backendProxyConfig.minSecurityLevel)
                        : null;
    }

    private String defaultPingPath() {
        String pingContextPath = removeContextPath ? "" : backendProxyConfig.contextPath;
        return pingContextPath + "/api/ping";
    }

    private String targetUrl(String requestURI) {
        return backendProxyConfig.baseUrl + targetPath(requestURI);
    }

    private String targetPath(String requestURI) {
        return removeContextPath ? requestURI.substring(contextPathLength) : requestURI;
    }

    public BackendProxyConfig getBackendProxyConfig() {
        return backendProxyConfig;
    }

    public String getId() {
        return id;
    }

    public String getPingUrl() {
        return pingUrl;
    }

    @Override
    protected HttpClient newHttpClient() {
        return new ProxyClient();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (isAuthorized(request, response)) {
            super.service(request, response);
        } else {
            response.setStatus(HttpStatus.UNAUTHORIZED_401);
        }
    }

    private boolean isAuthorized(HttpServletRequest request, HttpServletResponse response) {
        if (backendProxyConfig.validateOidcToken) {
            return loginService
                    .authenticate(request, response)
                    .filter(subject -> isValidSecurityLevel(subject, request))
                    .isPresent();
        } else {
            return true;
        }

    }

    private boolean isValidSecurityLevel(Subject subject, HttpServletRequest request) {
        if (securityLevelAuthorizationModule != null) {
            return securityLevelAuthorizationModule.authorized(subject, request);
        } else {
            return true;
        }
    }

    @Override
    protected String rewriteTarget(HttpServletRequest clientRequest) {

        StringBuilder sb = new StringBuilder(targetUrl(clientRequest.getRequestURI()));
        of(clientRequest.getQueryString()).ifPresent(q -> {
            sb.append("?");
            sb.append(q);
        });
        String target = sb.toString();

        String callId = LogFilter.resolveCallId(clientRequest);
        clientRequest.setAttribute(CALL_ID, callId);
        log.info(new ObjectAppendingMarker(MDCConstants.MDC_CALL_ID, callId), "{}", target);
        return target;
    }

    @Override
    protected void copyRequestHeaders(HttpServletRequest clientRequest, Request proxyRequest) {
        super.copyRequestHeaders(clientRequest, proxyRequest);
        if (nullOrEmpty(clientRequest.getHeader(CONSUMER_ID_HEADER_NAME))) {
            proxyRequest.header(CONSUMER_ID_HEADER_NAME, applicationName);
        }
        if (nullOrEmpty(clientRequest.getHeader(PREFERRED_NAV_CALL_ID_HEADER_NAME))) {
            proxyRequest.header(PREFERRED_NAV_CALL_ID_HEADER_NAME, (String) clientRequest.getAttribute(CALL_ID));
        }
    }

    @Override
    public void helsesjekk() throws Throwable {
        ContentResponse contentResponse = getHttpClient().GET(pingUrl);
        int status = contentResponse.getStatus();
        if (status != 200) {
            throw new IllegalStateException(status + " != 200");
        }
    }

    @Override
    public HelsesjekkMetadata getMetadata() {
        return helsesjekkMetadata;
    }

}
