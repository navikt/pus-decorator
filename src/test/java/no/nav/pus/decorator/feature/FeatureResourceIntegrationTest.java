package no.nav.pus.decorator.feature;

import lombok.SneakyThrows;
import no.nav.apiapp.ApiApp;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import no.nav.sbl.dialogarena.test.junit.SystemPropertiesRule;
import no.nav.testconfig.ApiAppTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.net.ServerSocket;
import java.util.Map;

import static no.nav.log.LogFilter.CONSUMER_ID_HEADER_NAME;
import static no.nav.log.LogFilter.PREFERRED_NAV_CALL_ID_HEADER_NAME;
import static no.nav.pus.decorator.DecoratorUtils.APPRES_CMS_URL_PROPERTY;
import static no.nav.pus.decorator.spa.SPAConfigResolver.WEBROOT_PATH_PROPERTY_NAME;
import static no.nav.pus.decorator.spa.SPAConfigResolverTest.getWebappSourceDirectory;
import static no.nav.sbl.rest.RestUtils.withClient;
import static org.assertj.core.api.Assertions.assertThat;

public class FeatureResourceIntegrationTest {

    @Rule
    public SystemPropertiesRule systemPropertiesRule = new SystemPropertiesRule();

    private Jetty jetty;

    private String applicationBasePath;

    @Before
    public void setup() throws Exception {
        int applicationPort = randomPort();

        String applicationName = getClass().getSimpleName();
        applicationBasePath = localBasePath(applicationPort) + "/" + applicationName;
        System.out.println("Application basepath:" + applicationBasePath);

        systemPropertiesRule.setProperty(APPRES_CMS_URL_PROPERTY, applicationBasePath);
        systemPropertiesRule.setProperty(WEBROOT_PATH_PROPERTY_NAME, getWebappSourceDirectory());
        systemPropertiesRule.setProperty("APPLICATION_NAME", applicationName);

        ApiAppTest.setupTestContext(ApiAppTest.Config.builder().applicationName(applicationName).build());
        jetty = ApiApp.startApiApp(ApplicationConfigTest.class, new String[]{Integer.toString(applicationPort)}).getJetty();
    }

    @After
    public void cleanup() {
        if (jetty != null) {
            jetty.stop.run();
        }
    }

    @Test
    public void testFeatureJson() {
        withClient(client -> {
            Response response = client.target(applicationBasePath).path("/api/feature")
                    .queryParam("feature", "testFeature1", "testFeature2")
                    .request()
                    .header(CONSUMER_ID_HEADER_NAME, "customConsumer")
                    .header(PREFERRED_NAV_CALL_ID_HEADER_NAME, "customCallId")
                    .get();

            assertThat(response.getStatus())
                    .isEqualTo(200);
            assertThat(response.getHeaderString("Content-Type")).isEqualTo("application/json;charset=utf-8");
            Map<?, ?> respData = response.readEntity(Map.class);
            assertThat(respData).hasSize(2);
            assertThat(respData.get("testFeature1")).isEqualTo(false);
            assertThat(respData.get("testFeature2")).isEqualTo(false);
            return null;
        });
    }

    private String localBasePath(int port) {
        return "http://localhost:" + port;
    }

    @SneakyThrows
    private int randomPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }
}
