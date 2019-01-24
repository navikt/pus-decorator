package no.nav.pus.decorator.config;

import lombok.SneakyThrows;
import no.nav.common.yaml.YamlUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;

import static no.nav.pus.decorator.config.ConfigResolver.resolveConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class ConfigResolverTest {

    @Test
    public void emptyConfig() {
        Config config = resolveConfig();
        assertThat(YamlUtils.toYaml(config)).isEqualTo(readResource("/config/default.config.yaml"));
    }

    @SneakyThrows
    private String readResource(String name) {
        InputStream resourceAsStream = ConfigResolverTest.class.getResourceAsStream(name);
        return IOUtils.toString(resourceAsStream, "UTF-8");
    }


}