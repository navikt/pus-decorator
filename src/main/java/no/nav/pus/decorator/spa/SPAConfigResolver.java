package no.nav.pus.decorator.spa;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.validation.ValidationUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static no.nav.json.JsonUtils.fromJsonArray;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

@Slf4j
public class SPAConfigResolver {

    public static final String DECORATOR_CONFIGURATION_PATH_PROPERTY_NAME = "DECORATOR_CONFIGURATION_PATH";
    public static final String WEBROOT_PATH_PROPERTY_NAME = "WEBROOT_PATH";

    public static List<SPAConfig> resolveSpaConfiguration() {
        return resolveSpaConfiguration(new File(getOptionalProperty(DECORATOR_CONFIGURATION_PATH_PROPERTY_NAME).orElse("/spa.config.json")));
    }

    static List<SPAConfig> resolveSpaConfiguration(File file) {
        if (file.exists()) {
            log.info("reading SPA configuration from {}", file.getAbsolutePath());
            return validate(parseDecoratorConfiguration(file));
        } else {
            log.info("no SPA configuration found at {}", file.getAbsolutePath());
            return Arrays.asList(
                    SPAConfig.builder()
                            .forwardTarget("/index.html")
                            .urlPattern("/*")
                            .build(),

                    SPAConfig.builder()
                            .forwardTarget("/demo/index.html")
                            .urlPattern("/demo/*")
                            .build()
            );
        }
    }

    private static List<SPAConfig> validate(List<SPAConfig> spaConfigs) {
        spaConfigs.forEach(ValidationUtils::validate);

        long uniqueUrlPatterns = spaConfigs.stream().map(spaConfig -> spaConfig.urlPattern).distinct().count();
        if (uniqueUrlPatterns != spaConfigs.size()) {
            throw new IllegalArgumentException("duplicate urlPatterns: " + spaConfigs);
        }

        spaConfigs.forEach(spaConfig->{
            File file = new File(getOptionalProperty(WEBROOT_PATH_PROPERTY_NAME).orElse("/app"), spaConfig.forwardTarget);
            if (!file.exists()) {
                throw new IllegalArgumentException(String.format("forwardTarget %s not found at: %s",
                        spaConfig.forwardTarget,
                        file.getAbsolutePath()
                ));
            } else {
                log.info("forwardTarget found {} at ", file.getAbsolutePath());
            }
        });
        return spaConfigs;
    }

    @SneakyThrows
    static List<SPAConfig> parseDecoratorConfiguration(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return fromJsonArray(fileInputStream, SPAConfig.class);
        }
    }
}
