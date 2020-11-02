package no.nav.pus.decorator.redirect;

import lombok.SneakyThrows;
import no.nav.apiapp.ApiApp;
import no.nav.common.yaml.YamlUtils;
import no.nav.pus.decorator.ApplicationConfig;
import no.nav.pus.decorator.config.Config;
import no.nav.pus.decorator.config.RedirectConfig;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import no.nav.sbl.dialogarena.test.junit.SystemPropertiesRule;
import no.nav.testconfig.ApiAppTest;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;

import static java.util.Arrays.asList;
import static no.nav.log.LogFilter.CONSUMER_ID_HEADER_NAME;
import static no.nav.log.LogFilter.PREFERRED_NAV_CALL_ID_HEADER_NAME;
import static no.nav.pus.decorator.DecoratorUtils.APPRES_CMS_URL_PROPERTY;
import static no.nav.pus.decorator.config.ConfigResolver.CONFIGURATION_LOCATION_PROPERTY;
import static no.nav.pus.decorator.spa.SPAConfigResolver.WEBROOT_PATH_PROPERTY_NAME;
import static no.nav.pus.decorator.spa.SPAConfigResolverTest.getWebappSourceDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.glassfish.jersey.client.ClientProperties.FOLLOW_REDIRECTS;


public class RedirectIntegrationTest {

    @Rule
    public SystemPropertiesRule systemPropertiesRule = new SystemPropertiesRule();

    private Jetty jetty;

    private String applicationName = getClass().getSimpleName();
    private String applicationBasePath;
    private JerseyClient client = JerseyClientBuilder.createClient();

    @Before
    public void setup() throws Exception {
        client.property(FOLLOW_REDIRECTS, false);

        int applicationPort = randomPort();
        applicationBasePath = localBasePath(applicationPort);
        File proxyConfigurationFile = writeConfiguration(
                new RedirectConfig()
                        .setFrom("/anotherapp")
                        .setTo(new URL("https://anotherapp.nav.no")),

                new RedirectConfig()
                        .setFrom("/nested/context")
                        .setTo(new URL("https://nested.nav.no/nested")),

                new RedirectConfig()
                        .setFrom("/nested/other/context")
                        .setTo(new URL("https://other.nav.no/nested"))
        );

        systemPropertiesRule.setProperty(CONFIGURATION_LOCATION_PROPERTY, proxyConfigurationFile.getAbsolutePath());
        systemPropertiesRule.setProperty(APPRES_CMS_URL_PROPERTY, "https://some-cms-here.com");
        systemPropertiesRule.setProperty(WEBROOT_PATH_PROPERTY_NAME, getWebappSourceDirectory());
        systemPropertiesRule.setProperty("APPLICATION_NAME", applicationName);

        ApiAppTest.setupTestContext(ApiAppTest.Config.builder()
                .environment("q1")
                .applicationName(applicationName).build());
        jetty = ApiApp.startApiApp(ApplicationConfig.class, new String[]{Integer.toString(applicationPort)}).getJetty();
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
    public void smoketest() {
        assertRedirect("/anotherapp", "https://anotherapp.nav.no/");
        assertRedirect("/anotherapp/", "https://anotherapp.nav.no/");
        assertRedirect("/anotherapp?a=b&c=d", "https://anotherapp.nav.no/?a=b&c=d");
        assertRedirect("/anotherapp/?a=b&c=d", "https://anotherapp.nav.no/?a=b&c=d");
        assertRedirect("/anotherapp/very/deep/path", "https://anotherapp.nav.no/very/deep/path");
        assertRedirect("/anotherapp/very/deep/path/", "https://anotherapp.nav.no/very/deep/path/");

        assertRedirect("/nested/context", "https://nested.nav.no/nested/");
        assertRedirect("/nested/context/", "https://nested.nav.no/nested/");
        assertRedirect("/nested/context/abc/def", "https://nested.nav.no/nested/abc/def");
        assertRedirect("/nested/other/context", "https://other.nav.no/nested/");
        assertRedirect("/nested/other/context/", "https://other.nav.no/nested/");
        assertRedirect("/nested/other/context/abc/def", "https://other.nav.no/nested/abc/def");
    }

    private void assertRedirect(String path, String target) {
        Response response = client.target(applicationBasePath + path).request()
                .header(CONSUMER_ID_HEADER_NAME, "customConsumer")
                .header(PREFERRED_NAV_CALL_ID_HEADER_NAME, "customCallId")
                .get();

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getLocation()).isEqualTo(URI.create(target));
    }


    private File writeConfiguration(RedirectConfig... redirectConfigs) throws IOException {
        File file = File.createTempFile(getClass().getSimpleName(), ".yaml");
        FileUtils.writeStringToFile(file, YamlUtils.toYaml(new Config().setRedirect(asList(redirectConfigs))), "UTF-8");
        return file;
    }

    @SneakyThrows
    private int randomPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

}
