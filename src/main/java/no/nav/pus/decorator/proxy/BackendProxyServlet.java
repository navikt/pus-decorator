package no.nav.pus.decorator.proxy;

import lombok.extern.slf4j.Slf4j;
import no.nav.apiapp.selftest.Helsesjekk;
import no.nav.apiapp.selftest.HelsesjekkMetadata;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.proxy.ProxyServlet;

import javax.servlet.http.HttpServletRequest;

import static no.nav.pus.decorator.proxy.BackendProxyConfig.RequestRewrite.REMOVE_CONTEXT_PATH;
import static no.nav.sbl.util.StringUtils.of;

@Slf4j
public class BackendProxyServlet extends ProxyServlet implements Helsesjekk {

    private final BackendProxyConfig backendProxyConfig;
    private final String id;
    private final HelsesjekkMetadata helsesjekkMetadata;
    private final String pingUrl;
    private final boolean removeContextPath;
    private final int contextPathLength;

    public BackendProxyServlet(BackendProxyConfig backendProxyConfig) {
        this.backendProxyConfig = backendProxyConfig;
        this.id = (BackendProxyServlet.class.getSimpleName() + "_" + backendProxyConfig.contextPath.substring(1)).toLowerCase();
        this.removeContextPath = backendProxyConfig.requestRewrite == REMOVE_CONTEXT_PATH;
        this.contextPathLength = backendProxyConfig.contextPath.length();

        this.pingUrl = targetUrl(backendProxyConfig.contextPath + backendProxyConfig.pingRequestPath);
        this.helsesjekkMetadata = new HelsesjekkMetadata(
                "proxy_" + id,
                pingUrl,
                "ping backend for " + backendProxyConfig.contextPath,
                false
        );
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
    protected String rewriteTarget(HttpServletRequest clientRequest) {
        StringBuilder sb = new StringBuilder(targetUrl(clientRequest.getRequestURI()));
        of(clientRequest.getQueryString()).ifPresent(q -> {
            sb.append("?");
            sb.append(q);
        });
        String target = sb.toString();
        log.info("{}", target);
        return target;
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
