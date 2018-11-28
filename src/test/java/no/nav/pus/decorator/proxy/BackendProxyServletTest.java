package no.nav.pus.decorator.proxy;

import no.nav.sbl.dialogarena.test.junit.SystemPropertiesRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

import static no.nav.pus.decorator.proxy.BackendProxyConfig.RequestRewrite.REMOVE_CONTEXT_PATH;
import static no.nav.sbl.util.EnvironmentUtils.APP_NAME_PROPERTY_NAME;
import static org.assertj.core.api.Assertions.assertThat;

public class BackendProxyServletTest {

    @Rule
    public SystemPropertiesRule systemPropertiesRule = new SystemPropertiesRule();

    @Before
    public void setup() {
        systemPropertiesRule.setProperty(APP_NAME_PROPERTY_NAME, "asdfasdf");
    }

    @Test
    public void defaultRewrite() throws MalformedURLException {
        BackendProxyServlet backendProxyServlet = new BackendProxyServlet(new BackendProxyConfig()
                .setContextPath("/context")
                .setBaseUrl(new URL("http://www.example.com"))
        );
        assertThat(backendProxyServlet.getPingUrl()).isEqualTo("http://www.example.com/context/api/ping");
        assertThat(backendProxyServlet.rewriteTarget(target("/context/request/path"))).isEqualTo("http://www.example.com/context/request/path");
    }

    @Test
    public void removeContextPath() throws MalformedURLException {
        BackendProxyServlet backendProxyServlet = new BackendProxyServlet(new BackendProxyConfig()
                .setContextPath("/context")
                .setBaseUrl(new URL("http://www.example.com"))
                .setRequestRewrite(REMOVE_CONTEXT_PATH)
        );
        assertThat(backendProxyServlet.getPingUrl()).isEqualTo("http://www.example.com/api/ping");
        assertThat(backendProxyServlet.rewriteTarget(target("/context/request/path"))).isEqualTo("http://www.example.com/request/path");
    }

    @Test
    public void customPing() throws MalformedURLException {
        BackendProxyServlet backendProxyServlet = new BackendProxyServlet(new BackendProxyConfig()
                .setContextPath("/context")
                .setBaseUrl(new URL("http://www.example.com"))
                .setPingRequestPath("/my/custom/ping")
        );
        assertThat(backendProxyServlet.getPingUrl()).isEqualTo("http://www.example.com/context/my/custom/ping");
        assertThat(backendProxyServlet.rewriteTarget(target("/context/request/path"))).isEqualTo("http://www.example.com/context/request/path");
    }

    private HttpServletRequest target(String path) {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setRequestURI(path);
        return mockHttpServletRequest;
    }

}