package no.nav.pus.decorator;


import no.nav.pus.decorator.proxy.BackendProxyConfig;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ApplicationConfigTest {

    @Test
    public void resolveProxyConfiguration() throws MalformedURLException {
        assertThat(ApplicationConfig.resolveProxyConfiguration(proxyJson("/demo-proxy.json"))).isEqualTo(asList(
                new BackendProxyConfig().setContextPath("/api").setBaseUrl(url("http://backend-api")),
                new BackendProxyConfig().setContextPath("/log").setBaseUrl(url("http://logger")),
                new BackendProxyConfig().setContextPath("/frontendlogger").setBaseUrl(url("http://frontendlogger")).setSkipCsrfProtection(true)
        ));
    }

    @Test
    public void resolveProxyConfiguration_invalidConfig() {
        assertThatThrownBy(() -> ApplicationConfig.resolveProxyConfiguration(proxyJson("/invalid-proxy.json")))
                .hasMessageContaining("baseUrl");
    }

    @Test
    public void resolveDecoratorConfiguration_defaul() {
        assertThat(ApplicationConfig.resolveDecoratorConfiguration()).isEqualTo(asList(
                "/index.html"
        ));
    }

    @Test
    public void resolveDecoratorConfiguration_nonExistingFile() {
        assertThat(ApplicationConfig.resolveDecoratorConfiguration(new File("/non-existing-file.json"))).isEqualTo(asList(
                "/index.html"
        ));
    }

    @Test
    public void resolveDecoratorConfiguration_withFile() {
        assertThat(ApplicationConfig.resolveDecoratorConfiguration(proxyJson("/demo-decorate.json"))).isEqualTo(asList(
                "/additional.html",
                "/path/*"
        ));
    }

    private File proxyJson(String name) {
        return new File(ApplicationConfigTest.class.getResource(name).getFile());
    }

    private URL url(String str) throws MalformedURLException {
        return URI.create(str).toURL();
    }

}