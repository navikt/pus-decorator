package no.nav.pus.decorator;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.SneakyThrows;
import no.nav.apiapp.ApiApp;
import no.nav.pus.decorator.proxy.BackendProxyConfig;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import no.nav.testconfig.ApiAppTest;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static no.nav.json.JsonUtils.toJson;
import static no.nav.pus.decorator.ApplicationConfig.PROXY_CONFIGURATION_PATH_PROPERTY_NAME;
import static no.nav.pus.decorator.DecoratorUtils.APPRES_CMS_URL_PROPERTY;
import static no.nav.sbl.dialogarena.test.SystemProperties.setTemporaryProperty;
import static no.nav.sbl.rest.RestUtils.withClient;
import static org.assertj.core.api.Assertions.assertThat;

public class ProxyIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    private Jetty jetty;
    private String applicationBasePath;

    @Before
    public void setup() throws Exception {
        int wiremockPort = wireMockRule.port();
        String wiremockBasePath = localBasePath(wiremockPort);
        givenThat(get(urlEqualTo("/proxy/teapot"))
                .willReturn(aResponse()
                        .withStatus(418)
                        .withBody("I'm a teapot!"))
        );

        int applicationPort = randomPort();
        applicationBasePath = localBasePath(applicationPort);
        File proxyConfigurationFile = writeProxyConfiguration(new BackendProxyConfig().setBaseUrl(new URL(wiremockBasePath)).setContextPath("/proxy"));

        setTemporaryProperty(PROXY_CONFIGURATION_PATH_PROPERTY_NAME, proxyConfigurationFile.getAbsolutePath(), () -> {
            setTemporaryProperty(APPRES_CMS_URL_PROPERTY, wiremockBasePath, () -> {

                ApiAppTest.setupTestContext(ApiAppTest.Config.builder().applicationName(getClass().getSimpleName()).build());
                jetty = ApiApp.startApiApp(ApplicationConfig.class, new String[]{Integer.toString(applicationPort)}).getJetty();
            });
        });
    }

    private String localBasePath(int port) {
        return "http://localhost:" + port;
    }

    @After
    public void cleanup() {
        if (jetty != null) {
            jetty.stop.run();
        }
    }

    @Test
    public void smoketest() throws Exception {

        withClient(client -> {
            assertThat(client.target(applicationBasePath).path("/proxy/teapot").request().get().getStatus()).isEqualTo(418);
            return null;
        });
    }

    @Test
    public void largeRequestHeader() throws Exception {
        String largeValue = IntStream.range(0, 6000).mapToObj(i -> "x").reduce("", (a, b) -> a + b);

        withClient(client -> {
            assertThat(client.target(applicationBasePath).path("/proxy/teapot").request()
                    .header("LARGE_HEADER", largeValue)
                    .get().getStatus()).isEqualTo(418);
            return null;
        });
    }

    private File writeProxyConfiguration(BackendProxyConfig... backendProxyConfigs) throws IOException {
        File file = File.createTempFile(getClass().getSimpleName(), ".json");
        FileUtils.writeStringToFile(file, toJson(asList(backendProxyConfigs)), "UTF-8");
        return file;
    }

    @SneakyThrows
    private int randomPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

}
