package no.nav.pus.decorator.proxy;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.pus.decorator.config.Config;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static no.nav.pus.decorator.ConfigurationService.Feature.FRONTEND_LOGGER;
import static no.nav.pus.decorator.ConfigurationService.isEnabled;

@Slf4j
public class ProxyConfigResolver {


    @SneakyThrows
    public static List<BackendProxyConfig> resolveProxyConfiguration(Config config) {
        List<BackendProxyConfig> backendProxyConfig = new ArrayList<>();
        ofNullable(config.proxy).ifPresent(backendProxyConfig::addAll);

        if (isEnabled(FRONTEND_LOGGER) && nothingOnFrontendloggerPath(backendProxyConfig)) {
            backendProxyConfig.add(new BackendProxyConfig()
                    .setBaseUrl(URI.create("http://frontendlogger").toURL())
                    .setContextPath("/frontendlogger")
                    .setSkipCsrfProtection(true) // unntak for frontendlogger
            );
        }

        log.info("proxy configuration: {}", backendProxyConfig);
        return backendProxyConfig;
    }

    private static boolean nothingOnFrontendloggerPath(List<BackendProxyConfig> backendProxyConfig) {
        return backendProxyConfig.stream().noneMatch(proxyConfig -> "/frontendlogger".equals(proxyConfig.contextPath));
    }

}
