package no.nav.pus.decorator.spa;

import lombok.extern.slf4j.Slf4j;
import no.nav.pus.decorator.config.Config;
import no.nav.pus.decorator.config.ConfigResolver;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.nav.pus.decorator.config.ConfigResolver.resolveConfig;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

@Slf4j
public class SPAConfigResolver {

    public static final String WEBROOT_PATH_PROPERTY_NAME = "WEBROOT_PATH";

    public static List<SPAConfig> resolveSpaConfiguration() {
        Config config = resolveConfig();
        List<SPAConfig> spa = config.spa == null || config.spa.isEmpty() ? defaultConfig() : config.spa;
        return validate(spa);
    }

    private static List<SPAConfig> defaultConfig() {
        log.info("no SPA configuration found, using default configuration");
        return Collections.singletonList(
                SPAConfig.builder()
                        .forwardTarget("/index.html")
                        .urlPattern("/*")
                        .build()
        );
    }

    private static List<SPAConfig> validate(List<SPAConfig> spaConfigs) {
        long uniqueUrlPatterns = spaConfigs.stream().map(SPAConfig::getUrlPattern).distinct().count();
        if (uniqueUrlPatterns != spaConfigs.size()) {
            throw new IllegalArgumentException("duplicate urlPatterns: " + spaConfigs);
        }

        spaConfigs.forEach(spaConfig -> {
            File file = new File(getOptionalProperty(WEBROOT_PATH_PROPERTY_NAME).orElse("/app"), spaConfig.getForwardTarget());
            if (!file.exists()) {
                throw new IllegalArgumentException(String.format("forwardTarget %s not found at: %s",
                        spaConfig.getForwardTarget(),
                        file.getAbsolutePath()
                ));
            } else {
                log.info("forwardTarget found {} at ", file.getAbsolutePath());
            }
        });
        return spaConfigs;
    }

}
