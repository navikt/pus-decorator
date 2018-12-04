package no.nav.pus.decorator.proxy;


import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProxyConfigResolverTest {

    @Test
    public void resolveProxyConfiguration() throws MalformedURLException {
        assertThat(ProxyConfigResolver.resolveProxyConfiguration(proxyJson("/demo-proxy.json"))).isEqualTo(asList(
                new BackendProxyConfig().setContextPath("/api").setBaseUrl(url("http://backend-api")),
                new BackendProxyConfig().setContextPath("/log").setBaseUrl(url("http://logger")),
                new BackendProxyConfig().setContextPath("/frontendlogger").setBaseUrl(url("http://frontendlogger")).setSkipCsrfProtection(true)
        ));
    }

    @Test
    public void resolveProxyConfiguration_invalidConfig() {
        assertThatThrownBy(() -> ProxyConfigResolver.resolveProxyConfiguration(proxyJson("/invalid-proxy.json")))
                .hasMessageContaining("baseUrl");

        assertThatThrownBy(() -> ProxyConfigResolver.resolveProxyConfiguration(proxyJson("/incomplete-proxy.json")))
                .hasMessageContaining("contextPath");
    }

    private File proxyJson(String name) {
        return new File(getClass().getResource(name).getFile());
    }

    private URL url(String str) throws MalformedURLException {
        return URI.create(str).toURL();
    }

}