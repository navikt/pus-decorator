package no.nav.pus.decorator.config;

import lombok.SneakyThrows;
import no.nav.common.yaml.YamlUtils;
import no.nav.pus.decorator.FooterType;
import no.nav.pus.decorator.HeaderType;
import no.nav.sbl.dialogarena.test.junit.SystemPropertiesRule;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;

import java.io.InputStream;

import static no.nav.pus.decorator.config.ConfigResolver.CONFIGURATION_LOCATION_PROPERTY;
import static no.nav.pus.decorator.config.ConfigResolver.resolveConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class ConfigResolverTest {

    @Rule
    public SystemPropertiesRule systemPropertiesRule = new SystemPropertiesRule();

    @Test
    public void emptyConfig() {
        Config config = resolveConfig();
        assertThat(YamlUtils.toYaml(config)).isEqualTo(readResource("/config/default.config.yaml"));
    }

    @Test
    public void richConfig() {
        systemPropertiesRule.setProperty(CONFIGURATION_LOCATION_PROPERTY, ConfigResolverTest.class.getResource("/config/rich.config.yaml").getFile());
        Config config = resolveConfig();
        assertThat(config).isEqualTo(new Config()
                .setContexPath("/custom-context-path")
                .setDecorator(new DecoratorConfig().setFooterType(FooterType.NO_FOOTER).setHeaderType(HeaderType.WITHOUT_MENU))
        );
    }

    @SneakyThrows
    private String readResource(String name) {
        InputStream resourceAsStream = ConfigResolverTest.class.getResourceAsStream(name);
        return IOUtils.toString(resourceAsStream, "UTF-8");
    }


}