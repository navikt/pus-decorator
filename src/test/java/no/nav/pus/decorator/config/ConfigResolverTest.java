package no.nav.pus.decorator.config;

import lombok.SneakyThrows;
import no.nav.common.yaml.YamlUtils;
import no.nav.pus.decorator.FooterType;
import no.nav.pus.decorator.HeaderType;
import no.nav.pus.decorator.TestUtils;
import no.nav.pus.decorator.login.AuthConfig;
import no.nav.pus.decorator.proxy.BackendProxyConfig;
import no.nav.pus.decorator.spa.SPAConfig;
import no.nav.sbl.dialogarena.test.junit.SystemPropertiesRule;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;

import static java.util.Arrays.asList;
import static no.nav.pus.decorator.config.ConfigResolver.CONFIGURATION_LOCATION_PROPERTY;
import static no.nav.pus.decorator.config.ConfigResolver.resolveConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConfigResolverTest {

    @Rule
    public SystemPropertiesRule systemPropertiesRule = new SystemPropertiesRule();

    @Test
    public void emptyConfig() {
        Config config = resolveConfig();
        assertThat(YamlUtils.toYaml(config)).isEqualTo(readResource("/config/default.config.yaml"));
    }

    @Test
    public void richConfig() throws Exception {
        systemPropertiesRule.setProperty(CONFIGURATION_LOCATION_PROPERTY, ConfigResolverTest.class.getResource("/config/rich.config.yaml").getFile());
        systemPropertiesRule.setProperty("API_URL", "http://my-api.com");
        systemPropertiesRule.setProperty("ENVIRONMENT", "t");
        systemPropertiesRule.setProperty(DecoratorConfig.FOOTER_TYPE_PROPERTY, FooterType.NO_FOOTER.name());

        Config config = resolveConfig();
        assertThat(config).isEqualTo(new Config()
                .setContextPath("/custom-context-path-t")
                .setDecorator(new DecoratorConfig().setFooterType(FooterType.NO_FOOTER).setHeaderType(HeaderType.WITHOUT_MENU))
                .setAuth(new AuthConfig()
                        .setEnforce(true)
                        .setLoginUrl(TestUtils.url("https://example.com"))
                )
                .setSpa(asList(
                        SPAConfig.builder().urlPattern("/app1").forwardTarget("/1.html").build()
                ))
                .setProxy(asList(
                        new BackendProxyConfig().setContextPath("/api").setBaseUrl(new URL("http://my-api.com")),
                        new BackendProxyConfig().setContextPath("/app").setBaseUrl(new URL("http://app-t.com"))
                ))
                .setRedirect(asList(
                        new RedirectConfig().setFrom("/home").setTo(new URL("https://www.nav.no"))
                ))
        );
    }

    @Test
    public void interpolate() {
        systemPropertiesRule.setProperty("A","1");
        systemPropertiesRule.setProperty("B","2");
        systemPropertiesRule.setProperty("C","3");
        systemPropertiesRule.setProperty("D","4");

        assertThat(ConfigResolver.interpolate("a:{{ A }} b:{{B}} c:{{ C}} d:{{D }}")).isEqualTo("a:1 b:2 c:3 d:4");
        assertThat(ConfigResolver.interpolate("{{ A }}{{B}}{{ C}}{{D }}")).isEqualTo("1234");
        assertThat(ConfigResolver.interpolate("abcd")).isEqualTo("abcd");

        assertThatThrownBy(() -> ConfigResolver.interpolate("{{ NOT_AVAILABLE_PROPERTY }}")).hasMessageContaining("NOT_AVAILABLE_PROPERTY");
    }

    @SneakyThrows
    private String readResource(String name) {
        InputStream resourceAsStream = ConfigResolverTest.class.getResourceAsStream(name);
        return IOUtils.toString(resourceAsStream, "UTF-8");
    }


}
