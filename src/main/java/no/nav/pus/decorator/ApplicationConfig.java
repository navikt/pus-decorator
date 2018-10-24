package no.nav.pus.decorator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.ServletUtil;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.brukerdialog.security.jaspic.OidcAuthModule;
import no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig;
import no.nav.brukerdialog.security.oidc.provider.AzureADB2CProvider;
import no.nav.innholdshenter.filter.DecoratorFilter;
import no.nav.pus.decorator.feature.ByQueryParamStrategy;
import no.nav.pus.decorator.feature.FeatureResource;
import no.nav.pus.decorator.login.LoginService;
import no.nav.pus.decorator.login.NoLoginService;
import no.nav.pus.decorator.login.OidcLoginService;
import no.nav.pus.decorator.proxy.BackendProxyConfig;
import no.nav.pus.decorator.proxy.BackendProxyServlet;
import no.nav.pus.decorator.spa.SPAConfig;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import no.nav.sbl.dialogarena.common.web.security.CsrfDoubleSubmitCookieFilter;
import no.nav.sbl.featuretoggle.unleash.UnleashService;
import no.nav.sbl.featuretoggle.unleash.UnleashServiceConfig;
import no.nav.validation.ValidationUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.inject.Provider;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static java.util.Collections.singletonList;
import static java.util.EnumSet.of;
import static java.util.stream.Collectors.toMap;
import static javax.servlet.DispatcherType.FORWARD;
import static javax.servlet.DispatcherType.REQUEST;
import static no.nav.apiapp.ServletUtil.leggTilFilter;
import static no.nav.apiapp.ServletUtil.leggTilServlet;
import static no.nav.json.JsonUtils.fromJsonArray;
import static no.nav.pus.decorator.ConfigurationService.Feature.DECORATOR;
import static no.nav.pus.decorator.ConfigurationService.Feature.PROXY;
import static no.nav.pus.decorator.ConfigurationService.isEnabled;
import static no.nav.pus.decorator.DecoratorUtils.getDecoratorFilter;
import static no.nav.pus.decorator.spa.SPAConfigResolver.resolveSpaConfiguration;
import static no.nav.sbl.featuretoggle.unleash.UnleashServiceConfig.UNLEASH_API_URL_PROPERTY_NAME;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Configuration
@Slf4j
public class ApplicationConfig implements ApiApplication.NaisApiApplication {

    public static final String APPLICATION_NAME_PROPERTY = "APPLICATION_NAME";
    public static final String NAIS_APP_NAME_PROPERTY_NAME = "APP_NAME";
    public static final String CONTEXT_PATH_PROPERTY_NAME = "CONTEXT_PATH";
    public static final String CONTENT_URL_PROPERTY_NAME = "CONTENT_URL";
    public static final String OIDC_LOGIN_URL_PROPERTY_NAME = "OIDC_LOGIN_URL";

    private static final String BACKEND_PROXY_CONTEXTS_PROPERTY_NAME = "PROXY_CONTEXTS";
    public static final String PROXY_CONFIGURATION_PATH_PROPERTY_NAME = "PROXY_CONFIGURATION_PATH";


    public static String resolveApplicationName() {
        return getRequiredProperty(APPLICATION_NAME_PROPERTY, NAIS_APP_NAME_PROPERTY_NAME);
    }

    @Override
    public boolean brukSTSHelsesjekk() {
        return false;
    }

    @Override
    public String getContextPath() {
        return getOptionalProperty(CONTEXT_PATH_PROPERTY_NAME).orElseGet(ApplicationConfig::resolveApplicationName);
    }

    @Override
    public void startup(ServletContext servletContext) {
        leggTilFilter(servletContext, CsrfDoubleSubmitCookieFilter.class);

        List<SPAConfig> spaConfigs = resolveSpaConfiguration();
        log.info("spa configuration: {}", spaConfigs);

        if(isEnabled(DECORATOR)){
            DecoratorFilter decoratorFilter = getDecoratorFilter();

            servletContext.addFilter("decoratorFilter", decoratorFilter)
                    .addMappingForUrlPatterns(EnumSet.of(FORWARD), false, spaConfigs.stream().map(s -> s.forwardTarget).toArray(String[]::new));
        }

        leggTilServlet(servletContext, EnvironmentServlet.class, "/environment.js");
        spaConfigs.forEach(spaConfig -> {
            ApplicationServlet servlet = new ApplicationServlet(
                    getOptionalProperty(OIDC_LOGIN_URL_PROPERTY_NAME).map(this::oidcLoginService).orElse(new NoLoginService()),
                    getOptionalProperty(CONTENT_URL_PROPERTY_NAME).orElse(null),
                    spaConfig.forwardTarget
            );
            ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(spaConfig.forwardTarget, servlet);
            servletRegistration.setLoadOnStartup(0);
            servletRegistration.addMapping(spaConfig.urlPattern);
            log.info("la til SPA under {} -> {}", spaConfig.urlPattern, spaConfig.forwardTarget);
        });


        if (isEnabled(PROXY)) {
            SingletonBeanRegistry singletonBeanRegistry = ((AnnotationConfigWebApplicationContext) ServletUtil.getContext(servletContext)).getBeanFactory();
            Collection<BackendProxyServlet> backendProxyServlets = (Collection<BackendProxyServlet>) servletContext.getAttribute(BACKEND_PROXY_CONTEXTS_PROPERTY_NAME);
            backendProxyServlets.forEach(backendProxyServlet -> singletonBeanRegistry.registerSingleton(backendProxyServlet.getId(), backendProxyServlet));
        }
    }

    private LoginService oidcLoginService(String oidcLoginUrl) {
        AzureADB2CConfig azureADB2CConfig = AzureADB2CConfig.readFromSystemProperties();
        AzureADB2CProvider azureADB2CProvider = new AzureADB2CProvider(azureADB2CConfig);
        OidcAuthModule oidcAuthModule = new OidcAuthModule(singletonList(azureADB2CProvider));
        return new OidcLoginService(oidcLoginUrl, oidcAuthModule);
    }

    @Bean
    @Conditional({ConfigurationService.DecoratorEnabled.class})
    public EnonicHelsesjekk enonicHelsesjekk() {
        return new EnonicHelsesjekk();
    }

    @Bean
    @Conditional({ConfigurationService.UnleashEnabled.class})
    public UnleashService unleashService(Provider<HttpServletRequest> httpServletRequestProvider) {
        return new UnleashService(UnleashServiceConfig.builder()
                .applicationName(ApplicationConfig.resolveApplicationName())
                .unleashApiUrl(getOptionalProperty(UNLEASH_API_URL_PROPERTY_NAME).orElse("https://unleashproxy.nais.oera.no/api/"))
                .build(),
                new ByQueryParamStrategy(httpServletRequestProvider)
        );
    }

    @Bean
    @Conditional({ConfigurationService.UnleashEnabled.class})
    public FeatureResource featureResource(UnleashService unleashService) {
        return new FeatureResource(unleashService);
    }

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
        if (isEnabled(PROXY)) {
            apiAppConfigurator.customizeJetty(this::addBackendProxies);
        }
    }

    private void addBackendProxies(Jetty jetty) {
        Map<String, BackendProxyServlet> backendProxyServlets = resolveProxyConfiguration()
                .stream()
                .collect(toMap(BackendProxyConfig::getContextPath, BackendProxyServlet::new));

        jetty.context.setAttribute(BACKEND_PROXY_CONTEXTS_PROPERTY_NAME, backendProxyServlets.values());

        Server server = jetty.server;
        HandlerCollection handlerCollection = new HandlerCollection();
        backendProxyServlets.forEach((contextPath, backendProxyServlet) -> handlerCollection.addHandler(proxyHandler(contextPath, backendProxyServlet)));
        handlerCollection.addHandler(server.getHandler());
        server.setHandler(handlerCollection);
    }

    public static List<BackendProxyConfig> resolveProxyConfiguration() {
        return resolveProxyConfiguration(new File(getOptionalProperty(PROXY_CONFIGURATION_PATH_PROPERTY_NAME).orElse("/proxy.json")));
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

    private static ServletContextHandler proxyHandler(String contextPath, BackendProxyServlet backendProxyServlet) {
        ServletContextHandler servletContextHandler = new ServletContextHandler();
        if (!backendProxyServlet.getBackendProxyConfig().isSkipCsrfProtection()) {
            servletContextHandler.addFilter(CsrfDoubleSubmitCookieFilter.class, "/*", of(REQUEST));
        }
        servletContextHandler.addServlet(new ServletHolder(backendProxyServlet), "/*");
        servletContextHandler.setContextPath(contextPath);
        return servletContextHandler;
    }


}
