package no.nav.pus.decorator;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.SneakyThrows;
import no.nav.apiapp.ApiApp;
import no.nav.common.yaml.YamlUtils;
import no.nav.pus.decorator.config.Config;
import no.nav.pus.decorator.login.AuthConfig;
import no.nav.pus.decorator.proxy.BackendProxyConfig;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import no.nav.sbl.dialogarena.test.junit.SystemPropertiesRule;
import no.nav.testconfig.ApiAppTest;
import no.nav.testconfig.security.JwtTestTokenIssuer;
import no.nav.testconfig.security.OidcProviderTestRule;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.util.SocketUtils;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.allRequests;
import static java.util.Arrays.asList;
import static no.nav.brukerdialog.security.SecurityLevel.Level3;
import static no.nav.brukerdialog.security.SecurityLevel.Level4;
import static no.nav.brukerdialog.security.oidc.OidcTokenUtils.SECURITY_LEVEL_ATTRIBUTE;
import static no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig.AZUREAD_B2C_DISCOVERY_URL_PROPERTY_NAME;
import static no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig.AZUREAD_B2C_EXPECTED_AUDIENCE_PROPERTY_NAME;
import static no.nav.log.LogFilter.CONSUMER_ID_HEADER_NAME;
import static no.nav.log.LogFilter.PREFERRED_NAV_CALL_ID_HEADER_NAME;
import static no.nav.pus.decorator.ApplicationConfig.CONTEXT_PATH_PROPERTY_NAME;
import static no.nav.pus.decorator.DecoratorUtils.APPRES_CMS_URL_PROPERTY;
import static no.nav.pus.decorator.config.ConfigResolver.CONFIGURATION_LOCATION_PROPERTY;
import static no.nav.pus.decorator.proxy.BackendProxyConfig.RequestRewrite.REMOVE_CONTEXT_PATH;
import static no.nav.pus.decorator.proxy.ProxyConfigResolver.FRONTENDLOGGER_URL_PROPERTY;
import static no.nav.pus.decorator.spa.SPAConfigResolver.WEBROOT_PATH_PROPERTY_NAME;
import static no.nav.pus.decorator.spa.SPAConfigResolverTest.getWebappSourceDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.glassfish.jersey.client.ClientProperties.FOLLOW_REDIRECTS;

public class ProxyIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Rule
    public OidcProviderTestRule oidcProviderRule = new OidcProviderTestRule(SocketUtils.findAvailableTcpPort());

    @Rule
    public SystemPropertiesRule systemPropertiesRule = new SystemPropertiesRule();

    private Jetty jetty;

    private String applicationName = getClass().getSimpleName();
    private String applicationBasePath;
    private int applicationPort;
    private JerseyClient client = JerseyClientBuilder.createClient();

    @Before
    public void setup() throws Exception {
        client.property(FOLLOW_REDIRECTS, false);

        int wiremockPort = wireMockRule.port();
        String wiremockBasePath = localBasePath(wiremockPort);

        givenThat(get(urlEqualTo("/frontendlogger"))
                .willReturn(aResponse()
                        .withStatus(234)
                        .withBody("all good!"))
        );

        givenThat(get(urlEqualTo("/proxy"))
                .willReturn(aResponse()
                        .withStatus(599)
                        .withBody("Unknown error!"))
        );

        givenThat(get(urlEqualTo("/proxy/teapot"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.IM_A_TEAPOT_418)
                        .withBody("I'm a teapot!"))
        );

        givenThat(get(urlPathMatching("\\/validate-oidc(-lvl4)?"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK_200)
                        .withBody("OK"))
        );

        applicationPort = randomPort();
        applicationBasePath = localBasePath(applicationPort);
        File proxyConfigurationFile = writeProxyConfiguration(
                new BackendProxyConfig()
                        .setBaseUrl(new URL(wiremockBasePath))
                        .setContextPath("/proxy")
                        .setValidateOidcToken(false)
                ,

                new BackendProxyConfig()
                        .setBaseUrl(new URL(wiremockBasePath))
                        .setContextPath("/remove-context-path")
                        .setRequestRewrite(REMOVE_CONTEXT_PATH)
                        .setValidateOidcToken(false)
                ,
                new BackendProxyConfig()
                        .setBaseUrl(new URL(wiremockBasePath))
                        .setContextPath("/validate-oidc")
                        .setValidateOidcToken(true)
                        .setMinSecurityLevel(0)
                ,
                new BackendProxyConfig()
                        .setBaseUrl(new URL(wiremockBasePath))
                        .setContextPath("/validate-oidc-lvl4")
                        .setValidateOidcToken(true)
                        .setMinSecurityLevel(4)
        );


        systemPropertiesRule.setProperty(CONFIGURATION_LOCATION_PROPERTY, proxyConfigurationFile.getAbsolutePath());
        systemPropertiesRule.setProperty(APPRES_CMS_URL_PROPERTY, wiremockBasePath);
        systemPropertiesRule.setProperty(WEBROOT_PATH_PROPERTY_NAME, getWebappSourceDirectory());
        systemPropertiesRule.setProperty(AZUREAD_B2C_DISCOVERY_URL_PROPERTY_NAME, oidcProviderRule.getDiscoveryUri());
        systemPropertiesRule.setProperty(AZUREAD_B2C_EXPECTED_AUDIENCE_PROPERTY_NAME, oidcProviderRule.getAudience());
        systemPropertiesRule.setProperty(FRONTENDLOGGER_URL_PROPERTY, wiremockBasePath);
        systemPropertiesRule.setProperty("APPLICATION_NAME", applicationName);

        ApiAppTest.setupTestContext(ApiAppTest.Config.builder().applicationName(applicationName).build());

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
    public void validOidcTokenRequest() {
        assertThat(
                client
                        .target(applicationBasePath)
                        .path("/validate-oidc")
                        .request()
                        .header("Authorization", "Bearer " + oidcProviderRule.getToken(
                                new JwtTestTokenIssuer.Claims("12345678901")))
                        .get()
                        .getStatus()
        ).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    public void invalidOidcTokenRequest() {
        assertThat(
                client
                        .target(applicationBasePath)
                        .path("/validate-oidc")
                        .request()
                        .get()
                        .getStatus()
        ).isEqualTo(HttpStatus.UNAUTHORIZED_401);
    }

    @Test
    public void validOidcTokenAuthorizeLevel4Request() {
        assertThat(
                client
                        .target(applicationBasePath)
                        .path("/validate-oidc-lvl4")
                        .request()
                        .header("Authorization", "Bearer " + oidcProviderRule.getToken(
                                new JwtTestTokenIssuer.Claims("12345678901").setClaim(SECURITY_LEVEL_ATTRIBUTE, Level4)))
                        .get()
                        .getStatus()
        ).isEqualTo(HttpStatus.OK_200);
    }

    @Test
    public void invalidOidcTokenAuthorizeLevel3Request() {
        assertThat(
                client
                        .target(applicationBasePath)
                        .path("/validate-oidc-lvl4")
                        .request()
                        .header("Authorization", "Bearer " + oidcProviderRule.getToken(
                                new JwtTestTokenIssuer.Claims("12345678901").setClaim(SECURITY_LEVEL_ATTRIBUTE, Level3)))
                        .get()
                        .getStatus()
        ).isEqualTo(HttpStatus.UNAUTHORIZED_401);
    }


    @Test
    public void frontendloggerShouldBePublic() {
        assertThat(
                client
                        .target(applicationBasePath)
                        .path("/frontendlogger")
                        .request()
                        .get()
                        .getStatus()
        ).isEqualTo(234);
    }

    @Test
    public void smoketest() {
        assertThat(client.target(applicationBasePath).path("/proxy").request().get().getStatus()).isEqualTo(599);
        assertThat(client.target(applicationBasePath).path("/proxy/teapot").request().get().getStatus()).isEqualTo(418);
        assertThat(client.target(applicationBasePath).path("/remove-context-path/proxy/teapot").request().get().getStatus()).isEqualTo(418);

        verify(allRequests().withHeader(CONSUMER_ID_HEADER_NAME, equalTo(applicationName)));
        verify(allRequests().withHeader(PREFERRED_NAV_CALL_ID_HEADER_NAME, matching("\\w{32}")));
    }

    @Test
    public void smoketest__no_context_path() {
        cleanup();
        systemPropertiesRule.setProperty(CONTEXT_PATH_PROPERTY_NAME, "/");

        jetty = ApiApp.startApiApp(ApplicationConfig.class, new String[]{Integer.toString(applicationPort)}).getJetty();
        smoketest();
    }

    @Test
    public void largeRequestHeader() {
        String largeValue = IntStream.range(0, 6000).mapToObj(i -> "x").reduce("", (a, b) -> a + b);

        assertThat(client.target(applicationBasePath).path("/proxy/teapot").request()
                .header("LARGE_HEADER", largeValue)
                .get().getStatus()).isEqualTo(418);
    }

    @Test
    public void customCorrelationIds() {
        client.target(applicationBasePath).path("/proxy/teapot").request()
                .header(CONSUMER_ID_HEADER_NAME, "customConsumer")
                .header(PREFERRED_NAV_CALL_ID_HEADER_NAME, "customCallId")
                .get();

        verify(allRequests().withHeader(CONSUMER_ID_HEADER_NAME, equalTo("customConsumer")));
        verify(allRequests().withHeader(PREFERRED_NAV_CALL_ID_HEADER_NAME, equalTo("customCallId")));
    }

    private File writeProxyConfiguration(BackendProxyConfig... backendProxyConfigs) throws IOException {
        File file = File.createTempFile(getClass().getSimpleName(), ".json");
        Config config = new Config()
                .setAuth(new AuthConfig().setLoginUrl(TestUtils.url("http://localhost/login")))
                .setProxy(asList(backendProxyConfigs));
        FileUtils.writeStringToFile(file, YamlUtils.toYaml(config), "UTF-8");
        return file;
    }

    @SneakyThrows
    private int randomPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

}
