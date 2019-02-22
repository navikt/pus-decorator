package no.nav.pus.decorator.proxy;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.pus.decorator.config.Config;
import no.nav.sbl.util.EnvironmentUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static no.nav.pus.decorator.ConfigurationService.Feature.FRONTEND_LOGGER;
import static no.nav.pus.decorator.ConfigurationService.isEnabled;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

@Slf4j
public class ProxyConfigResolver {

    public static final String FRONTENDLOGGER_URL_PROPERTY = "FRONTENDLOGGER_URL";

    @SneakyThrows
    public static List<BackendProxyConfig> resolveProxyConfiguration(Config config) {
        List<BackendProxyConfig> backendProxyConfig = new ArrayList<>();
        ofNullable(config.proxy).ifPresent(backendProxyConfig::addAll);

        if (isEnabled(FRONTEND_LOGGER) && nothingOnFrontendloggerPath(backendProxyConfig)) {
            String frontendloggerUrl = getOptionalProperty(FRONTENDLOGGER_URL_PROPERTY).orElse("http://frontendlogger");
            backendProxyConfig.add(new BackendProxyConfig()
                    .setBaseUrl(URI.create(frontendloggerUrl).toURL())
                    .setContextPath("/frontendlogger")

                    // unntak for frontendlogger siden den skal være public
                    .setSkipCsrfProtection(true)
                    .setValidateOidcToken(false)
            );
        }

        log.info("proxy configuration: {}", backendProxyConfig);
        return backendProxyConfig;
    }

    private static boolean nothingOnFrontendloggerPath(List<BackendProxyConfig> backendProxyConfig) {
        return backendProxyConfig.stream().noneMatch(proxyConfig -> "/frontendlogger".equals(proxyConfig.contextPath));
    }

}
