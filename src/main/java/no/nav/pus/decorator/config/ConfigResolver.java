package no.nav.pus.decorator.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.yaml.YamlUtils;
import no.nav.sbl.util.EnvironmentUtils;
import no.nav.validation.ValidationUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;

import static no.nav.pus.decorator.config.DeprecatedConfigDetector.checkForDeprecatedConfig;

@Slf4j
public class ConfigResolver {

    public static final String CONFIGURATION_LOCATION_PROPERTY = "CONFIGURATION_LOCATION";

    public static Config resolveConfig() {
        checkForDeprecatedConfig();
        Config object = doResolveConfig();
        log.info("{}", object);
        return ValidationUtils.validate(object);
    }

    @SneakyThrows
    private static Config doResolveConfig() {
        String configurationLocation = EnvironmentUtils.getOptionalProperty(CONFIGURATION_LOCATION_PROPERTY).orElse("/decorator.yaml");
        File file = new File(configurationLocation);
        if (file.exists()) {
            log.info("Reading configuration file at: {}", configurationLocation);
            return YamlUtils.fromYaml(FileUtils.readFileToString(file, "UTF-8"), Config.class);
        } else {
            log.info("No configuration found at {}, using default configuration", configurationLocation);
            return new Config();
        }
    }

}
