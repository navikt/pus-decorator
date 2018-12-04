package no.nav.pus.decorator.proxy;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.pus.decorator.ApplicationConfig;
import no.nav.validation.ValidationUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static no.nav.json.JsonUtils.fromJsonArray;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

@Slf4j
public class ProxyConfigResolver {

    public static List<BackendProxyConfig> resolveProxyConfiguration() {
        return resolveProxyConfiguration(new File(getOptionalProperty(ApplicationConfig.PROXY_CONFIGURATION_PATH_PROPERTY_NAME).orElse("/proxy.json")));
    }

    @SneakyThrows
    static List<BackendProxyConfig> resolveProxyConfiguration(File file) {
        List<BackendProxyConfig> backendProxyConfig = new ArrayList<>();
        if (file.exists()) {
            log.info("reading proxy configuration from {}", file.getAbsolutePath());
            backendProxyConfig.addAll(parseProxyConfiguration(file));
        } else {
            log.info("no proxy configuration found at {}", file.getAbsolutePath());
        }

        if (backendProxyConfig.stream().noneMatch(proxyConfig -> "/frontendlogger".equals(proxyConfig.contextPath))) {
            backendProxyConfig.add(new BackendProxyConfig()
                    .setBaseUrl(URI.create("http://frontendlogger").toURL())
                    .setContextPath("/frontendlogger")
                    .setSkipCsrfProtection(true) // unntak for frontendlogger
            );
        }

        log.info("proxy configuration: {}", backendProxyConfig);
        return backendProxyConfig;
    }

    private static List<BackendProxyConfig> parseProxyConfiguration(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            List<BackendProxyConfig> backendProxyConfigs = fromJsonArray(fileInputStream, BackendProxyConfig.class);
            backendProxyConfigs.forEach(ValidationUtils::validate);
            return backendProxyConfigs;
        }
    }

}
