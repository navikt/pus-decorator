package no.nav.pus.decorator;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.ServletUtil;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.pus.decorator.feature.FeatureResource;
import no.nav.sbl.dialogarena.common.web.security.CsrfDoubleSubmitCookieFilter;
import no.nav.sbl.featuretoggle.unleash.UnleashService;
import no.nav.sbl.featuretoggle.unleash.UnleashServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.servlet.ServletContext;
import java.util.EnumSet;

import static javax.servlet.DispatcherType.FORWARD;
import static no.nav.apiapp.ServletUtil.leggTilFilter;
import static no.nav.apiapp.ServletUtil.leggTilServlet;
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
    public static final String CONTEXT_PATH_PROPERTY_NAME = "CONTEXT_PATH";
    public static final String APPLICATION_NAME = getRequiredProperty(APPLICATION_NAME_PROPERTY, NAIS_APP_NAME_PROPERTY_NAME);

    @Override
    public boolean brukSTSHelsesjekk() {
        return false;
    }

    @Override
    public String getContextPath() {
        return getOptionalProperty(CONTEXT_PATH_PROPERTY_NAME).orElse(APPLICATION_NAME);
    }

    @Override
    public void startup(ServletContext servletContext) {
        leggTilFilter(servletContext,CsrfDoubleSubmitCookieFilter.class);

        servletContext.addFilter("decoratorFilter", getDecoratorFilter())
                .addMappingForUrlPatterns(EnumSet.of(FORWARD), false, "/index.html");

        leggTilServlet(servletContext, EnvironmentServlet.class, "/environment.js");
        leggTilServlet(servletContext, ApplicationServlet.class, "/*");
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
