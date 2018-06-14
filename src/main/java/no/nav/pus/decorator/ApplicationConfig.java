package no.nav.pus.decorator;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.pus.decorator.feature.FeatureResource;
import no.nav.sbl.dialogarena.common.web.security.CsrfDoubleSubmitCookieFilter;
import no.nav.sbl.featuretoggle.unleash.UnleashService;
import no.nav.sbl.featuretoggle.unleash.UnleashServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.EnumSet;

import static no.nav.pus.decorator.DecoratorUtils.getDecoratorFilter;
import static no.nav.sbl.featuretoggle.unleash.UnleashServiceConfig.UNLEASH_API_URL_PROPERTY_NAME;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Configuration
@Import({
        EnonicHelsesjekk.class,
        FeatureResource.class
})
public class ApplicationConfig implements ApiApplication.NaisApiApplication {

    public static final String APPLICATION_NAME_PROPERTY = "APPLICATION_NAME";
    public static final String NAIS_APP_NAME_PROPERTY_NAME = "APP_NAME";
    public static final String APPLICATION_NAME = getRequiredProperty(APPLICATION_NAME_PROPERTY, NAIS_APP_NAME_PROPERTY_NAME);

    @Override
    public String getApplicationName() {
        return APPLICATION_NAME;
    }

    @Override
    public boolean brukSTSHelsesjekk() {
        return false;
    }

    @Override
    public void startup(ServletContext servletContext) {
        FilterRegistration.Dynamic dynamic = servletContext.addFilter("csrf", CsrfDoubleSubmitCookieFilter.class);
        dynamic.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");

        FilterRegistration.Dynamic docratorfilter = servletContext.addFilter("docratorfilter", getDecoratorFilter());
        docratorfilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.FORWARD), false, "/index.html");

        ServletRegistration.Dynamic enviorment = servletContext.addServlet("environment", new EnvironmentServlet());
        enviorment.addMapping("/environment.js");

        ServletRegistration.Dynamic reactapp = servletContext.addServlet("reactapp", new ApplicationServlet());
        reactapp.addMapping("/*");
    }


    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {

    }

    @Bean
    public UnleashService unleashService() {
        return new UnleashService(UnleashServiceConfig.builder()
                .applicationName(ApplicationConfig.APPLICATION_NAME)
                .unleashApiUrl(getOptionalProperty(UNLEASH_API_URL_PROPERTY_NAME).orElse("https://unleashproxy.nais.oera.no/api/"))
                .build()
        );
    }

}
