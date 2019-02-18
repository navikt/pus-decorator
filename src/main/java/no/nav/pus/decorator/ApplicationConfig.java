package no.nav.pus.decorator;

import lombok.extern.slf4j.Slf4j;
import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.ServletUtil;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.brukerdialog.security.jaspic.OidcAuthModule;
import no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig;
import no.nav.brukerdialog.security.oidc.provider.AzureADB2CProvider;
import no.nav.innholdshenter.filter.DecoratorFilter;
import no.nav.pus.decorator.config.Config;
import no.nav.pus.decorator.feature.ByApplicationStrategy;
import no.nav.pus.decorator.feature.ByQueryParamStrategy;
import no.nav.pus.decorator.feature.FeatureResource;
import no.nav.pus.decorator.login.AuthenticationResource;
import no.nav.pus.decorator.login.LoginService;
import no.nav.pus.decorator.login.NoLoginService;
import no.nav.pus.decorator.login.OidcLoginService;
import no.nav.pus.decorator.proxy.BackendProxyConfig;
import no.nav.pus.decorator.proxy.BackendProxyServlet;
import no.nav.pus.decorator.redirect.RedirectServlet;
import no.nav.pus.decorator.spa.SPAConfig;
import no.nav.sbl.dialogarena.common.web.security.CsrfDoubleSubmitCookieFilter;
import no.nav.sbl.featuretoggle.unleash.UnleashService;
import no.nav.sbl.featuretoggle.unleash.UnleashServiceConfig;
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.EnumSet.of;
import static java.util.Optional.ofNullable;
import static javax.servlet.DispatcherType.FORWARD;
import static javax.servlet.DispatcherType.REQUEST;
import static no.nav.apiapp.ServletUtil.leggTilFilter;
import static no.nav.apiapp.ServletUtil.leggTilServlet;
import static no.nav.pus.decorator.ConfigurationService.Feature.*;
import static no.nav.pus.decorator.ConfigurationService.isEnabled;
import static no.nav.pus.decorator.DecoratorUtils.getDecoratorFilter;
import static no.nav.pus.decorator.config.ConfigResolver.resolveConfig;
import static no.nav.pus.decorator.proxy.ProxyConfigResolver.resolveProxyConfiguration;
import static no.nav.pus.decorator.spa.SPAConfigResolver.resolveSpaConfiguration;
import static no.nav.sbl.featuretoggle.unleash.UnleashServiceConfig.UNLEASH_API_URL_PROPERTY_NAME;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Configuration
@Slf4j
public class ApplicationConfig implements ApiApplication {

    public static final String APPLICATION_NAME_PROPERTY = "APPLICATION_NAME";
    public static final String NAIS_APP_NAME_PROPERTY_NAME = "APP_NAME";
    public static final String CONTEXT_PATH_PROPERTY_NAME = "CONTEXT_PATH";
    public static final String CONTENT_URL_PROPERTY_NAME = "CONTENT_URL";
    public static final String OIDC_LOGIN_URL_PROPERTY_NAME = "OIDC_LOGIN_URL";

    private LoginService loginService = getOptionalProperty(OIDC_LOGIN_URL_PROPERTY_NAME)
            .map(this::oidcLoginService).orElse(new NoLoginService());

    private final Config config = resolveConfig();

    public static String resolveApplicationName() {
        return getRequiredProperty(APPLICATION_NAME_PROPERTY, NAIS_APP_NAME_PROPERTY_NAME);
    }

    @Override
    public String getContextPath() {
        return getOptionalProperty(CONTEXT_PATH_PROPERTY_NAME).orElseGet(ApplicationConfig::resolveApplicationName);
    }

    @Override
    public void startup(ServletContext servletContext) {
        leggTilFilter(servletContext, CsrfDoubleSubmitCookieFilter.class);

        List<SPAConfig> spaConfigs = resolveSpaConfiguration(config);
        log.info("spa configuration: {}", spaConfigs);

        if(isEnabled(DECORATOR)){
            DecoratorFilter decoratorFilter = getDecoratorFilter(config.decorator);

            servletContext.addFilter("decoratorFilter", decoratorFilter)
                    .addMappingForUrlPatterns(EnumSet.of(FORWARD), false, spaConfigs.stream().map(SPAConfig::getForwardTarget).toArray(String[]::new));
        }

        leggTilServlet(servletContext, EnvironmentServlet.class, "/environment.js");

        AnnotationConfigWebApplicationContext annotationConfigWebApplicationContext = (AnnotationConfigWebApplicationContext) ServletUtil.getContext(servletContext);
        SingletonBeanRegistry singletonBeanRegistry = annotationConfigWebApplicationContext.getBeanFactory();

        singletonBeanRegistry.registerSingleton(AuthenticationResource.class.getName(), new AuthenticationResource(loginService, servletContext.getContextPath()));

        spaConfigs.forEach(spaConfig -> {
            String forwardTarget = spaConfig.getForwardTarget();
            String urlPattern = spaConfig.getUrlPattern();
            ApplicationServlet servlet = new ApplicationServlet(
                    loginService,
                    getOptionalProperty(CONTENT_URL_PROPERTY_NAME).orElse(null),
                    forwardTarget,
                    isEnabled(UNLEASH) ? annotationConfigWebApplicationContext.getBean(UnleashService.class) : null
            );
            ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(urlPattern, servlet);
            servletRegistration.setLoadOnStartup(0);
            servletRegistration.addMapping(urlPattern);
            log.info("la til SPA under {} -> {}", urlPattern, forwardTarget);
        });
    }

    private LoginService oidcLoginService(String oidcLoginUrl) {
        AzureADB2CConfig azureADB2CConfig = AzureADB2CConfig.readFromSystemProperties();
        AzureADB2CProvider azureADB2CProvider = new AzureADB2CProvider(azureADB2CConfig);
        OidcAuthModule oidcAuthModule = new OidcAuthModule(singletonList(azureADB2CProvider));
        return new OidcLoginService(oidcLoginUrl, oidcAuthModule, getContextPath());
    }

    @Bean
    @Conditional({ConfigurationService.DecoratorEnabled.class})
    public EnonicHelsesjekk enonicHelsesjekk() {
        return new EnonicHelsesjekk(config.decorator);
    }

    @Bean
    @Conditional({ConfigurationService.UnleashEnabled.class})
    public UnleashService unleashService(Provider<HttpServletRequest> httpServletRequestProvider) {
        return new UnleashService(UnleashServiceConfig.builder()
                .applicationName(ApplicationConfig.resolveApplicationName())
                .unleashApiUrl(getOptionalProperty(UNLEASH_API_URL_PROPERTY_NAME).orElse("https://unleashproxy.nais.oera.no/api/"))
                .build(),
                new ByQueryParamStrategy(httpServletRequestProvider),
                new ByApplicationStrategy()
        );
    }

    @Bean
    @Conditional({ConfigurationService.UnleashEnabled.class})
    public FeatureResource featureResource(UnleashService unleashService) {
        return new FeatureResource(unleashService);
    }

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
        apiAppConfigurator.customizeJetty(jetty -> {
            HandlerCollection handlerCollection = new HandlerCollection();
            if (isEnabled(PROXY)) {
                addBackendProxies(apiAppConfigurator, handlerCollection, loginService);
            }

            addRedirects(handlerCollection);

            Server server = jetty.server;
            handlerCollection.addHandler(server.getHandler());
            server.setHandler(handlerCollection);
        });
    }

    private void addRedirects(HandlerCollection handlerCollection) {
        ofNullable(config.getRedirect()).orElseGet(Collections::emptyList).forEach(redirectConfig -> {
            ServletContextHandler servletContextHandler = new ServletContextHandler();
            servletContextHandler.setAllowNullPathInfo(true);
            servletContextHandler.addServlet(new ServletHolder(new RedirectServlet(redirectConfig)), "/*");
            servletContextHandler.setContextPath(redirectConfig.getFrom());
            handlerCollection.addHandler(servletContextHandler);
            log.info("redirect: {} -> {}",
                    redirectConfig.from,
                    redirectConfig.to
            );
        });
    }

    private void addBackendProxies(ApiAppConfigurator apiAppConfigurator, HandlerCollection handlerCollection, LoginService loginService) {
        resolveProxyConfiguration(config)
                .stream()
                .map((BackendProxyConfig backendProxyConfig) -> new BackendProxyServlet(backendProxyConfig, loginService))
                .forEach(backendProxyServlet -> {
                    handlerCollection.addHandler(proxyHandler(backendProxyServlet));
                    apiAppConfigurator.selfTest(backendProxyServlet);
                });
    }

    private static ServletContextHandler proxyHandler(BackendProxyServlet backendProxyServlet) {
        BackendProxyConfig backendProxyConfig = backendProxyServlet.getBackendProxyConfig();
        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setAllowNullPathInfo(true);
        if (!backendProxyConfig.isSkipCsrfProtection()) {
            servletContextHandler.addFilter(CsrfDoubleSubmitCookieFilter.class, "/*", of(REQUEST));
        }
        servletContextHandler.addServlet(new ServletHolder(backendProxyServlet), "/*");
        servletContextHandler.setContextPath(backendProxyConfig.getContextPath());
        return servletContextHandler;
    }


}
