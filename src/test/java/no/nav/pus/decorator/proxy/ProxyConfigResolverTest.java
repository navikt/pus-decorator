package no.nav.pus.decorator.proxy;


import no.nav.pus.decorator.config.ConfigResolver;
import no.nav.sbl.dialogarena.test.junit.SystemPropertiesRule;
import org.junit.Rule;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProxyConfigResolverTest {

    @Rule
    public SystemPropertiesRule systemPropertiesRule = new SystemPropertiesRule();


    @Test
    public void resolveProxyConfiguration() throws MalformedURLException {
        assertThat(resolveProxyConfiguration("/config/demo-proxy.yaml")).isEqualTo(asList(
                new BackendProxyConfig().setContextPath("/api").setBaseUrl(url("http://backend-api")),
                new BackendProxyConfig().setContextPath("/log").setBaseUrl(url("http://logger")),
                new BackendProxyConfig().setContextPath("/frontendlogger").setBaseUrl(url("http://frontendlogger")).setSkipCsrfProtection(true)
        ));
    }

    @Test
    public void resolveProxyConfiguration_invalidConfig() {
        assertThatThrownBy(() -> resolveProxyConfiguration("/config/invalid-proxy.yaml"))
                .hasMessageContaining("baseUrl");

        assertThatThrownBy(() -> resolveProxyConfiguration("/config/incomplete-proxy.yaml"))
                .hasMessageContaining("contextPath");
    }

    private URL url(String str) throws MalformedURLException {
        return URI.create(str).toURL();
    }

    private List<BackendProxyConfig> resolveProxyConfiguration(String name){
        systemPropertiesRule.setProperty(ConfigResolver.CONFIGURATION_LOCATION_PROPERTY,getClass().getResource(name).getFile());
        return ProxyConfigResolver.resolveProxyConfiguration();
    }

}