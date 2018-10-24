package no.nav.pus.decorator.spa;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static no.nav.json.JsonUtils.fromJsonArray;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

@Slf4j
public class SPAConfigResolver {

    private static final String DECORATOR_CONFIGURATION_PATH_PROPERTY_NAME = "DECORATOR_CONFIGURATION_PATH";

    public static List<SPAConfig> resolveSpaConfiguration() {
        return resolveSpaConfiguration(new File(getOptionalProperty(DECORATOR_CONFIGURATION_PATH_PROPERTY_NAME).orElse("/spa.config.json")));
    }

    @SneakyThrows
    static List<SPAConfig> resolveSpaConfiguration(File file) {
        if (file.exists()) {
            log.info("reading SPA configuration from {}", file.getAbsolutePath());
            return parseDecoratorConfiguration(file);
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

    private static List<SPAConfig> parseDecoratorConfiguration(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return fromJsonArray(fileInputStream, SPAConfig.class);
        }
    }
}
