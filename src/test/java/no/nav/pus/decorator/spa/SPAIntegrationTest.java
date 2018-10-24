package no.nav.pus.decorator.spa;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.SneakyThrows;
import no.nav.apiapp.ApiApp;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.pus.decorator.ApplicationConfig;
import no.nav.pus.decorator.FragmentConfig;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import no.nav.sbl.dialogarena.test.junit.SystemPropertiesRule;
import no.nav.testconfig.ApiAppTest;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.net.ServerSocket;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static no.nav.json.JsonUtils.toJson;
import static no.nav.pus.decorator.DecoratorUtils.APPRES_CMS_URL_PROPERTY;
import static no.nav.pus.decorator.spa.SPAConfigResolver.DECORATOR_CONFIGURATION_PATH_PROPERTY_NAME;
import static no.nav.sbl.rest.RestUtils.withClient;
import static org.assertj.core.api.Assertions.assertThat;

public class SPAIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Rule
    public SystemPropertiesRule systemPropertiesRule = new SystemPropertiesRule();

    private String applicationBasePath;
    private Jetty jetty;

    @Before
    public void setup() {
        int wiremockPort = wireMockRule.port();
        String wiremockBasePath = localBasePath(wiremockPort) + "/enonic";

        givenThat(get(urlPathEqualTo("/enonic/common-html/v4/navno"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("<html><body><div id=\"" + FragmentConfig.HEADER_TYPE.getFragmentName().get() + "\"><h1>decorated!<h1></div></body></html>"))
        );

        String applicationName = getClass().getSimpleName();

        int applicationPort = randomPort();
        applicationBasePath = localBasePath(applicationPort) + "/" + applicationName;

        File proxyConfigurationFile = writeSPAConfiguration(
                SPAConfig.builder()
                        .forwardTarget("/root.html")
                        .urlPattern("/")
                        .build(),

                SPAConfig.builder()
                        .forwardTarget("/app1.html")
                        .urlPattern("/app1/*")
                        .build(),

                SPAConfig.builder()
                        .forwardTarget("/app2/app2.html")
                        .urlPattern("/app2/*")
                        .build(),

                SPAConfig.builder()
                        .forwardTarget("/app1.html")
                        .urlPattern("/app3/*")
                        .build()

        );

        systemPropertiesRule.setProperty(DECORATOR_CONFIGURATION_PATH_PROPERTY_NAME, proxyConfigurationFile.getAbsolutePath());
        systemPropertiesRule.setProperty(APPRES_CMS_URL_PROPERTY, wiremockBasePath);

        ApiAppTest.setupTestContext(ApiAppTest.Config.builder().applicationName(applicationName).build());
        jetty = ApiApp.startApiApp(SPAIntegrationTestConfig.class, new String[]{Integer.toString(applicationPort)}).getJetty();
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
            pathResponsWithContent("/", "root");
            pathResponsWithContent("/deep/path/for/root", "root");

            pathResponsWithContent("/app1", "app1");
            pathResponsWithContent("/app1/", "app1");
            pathResponsWithContent("/app1/deep/path/for/app1", "app1");

            pathResponsWithContent("/app2", "app2");
            pathResponsWithContent("/app2/", "app2");
            pathResponsWithContent("/app2/deep/path/for/app2", "app2");

            pathResponsWithContent("/app3", "app1");
            pathResponsWithContent("/app3/", "app1");
            pathResponsWithContent("/app3/deep/path/for/app3", "app1");
            return null;
        });
    }

    private void pathResponsWithContent(String path, String expectedContent) {
        withClient(client -> {
            String response = client.target(applicationBasePath)
                    .path(path)
                    .request()
                    .get(String.class);

            assertThat(response)
                    .contains(expectedContent)
                    .contains("decorated!");
            return null;
        });
    }

    @SneakyThrows
    private File writeSPAConfiguration(SPAConfig... spaConfigs) {
        File file = File.createTempFile(getClass().getSimpleName(), ".json");
        FileUtils.writeStringToFile(file, toJson(asList(spaConfigs)), "UTF-8");
        return file;
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

    @Configuration
    public static class SPAIntegrationTestConfig extends ApplicationConfig {
        @Override
        public void configure(ApiAppConfigurator apiAppConfigurator) {
            super.configure(apiAppConfigurator);
            apiAppConfigurator.customizeJettyBuilder(jettyBuilder -> {
                jettyBuilder.war(new File(SPAIntegrationTest.class.getResource("/spaIntegrationTest").getFile()));
            });
        }
    }
}
