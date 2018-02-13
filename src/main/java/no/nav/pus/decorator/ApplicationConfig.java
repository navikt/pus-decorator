package no.nav.pus.decorator;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.EnumSet;

import static no.nav.apiapp.ApiApplication.Sone.SBS;
import static no.nav.pus.decorator.DecoratorUtils.getDecoratorFilter;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Configuration
@Import(EnonicHelsesjekk.class)
public class ApplicationConfig implements ApiApplication.NaisApiApplication {

    public static final String APPLICATION_NAME_PROPERTY = "APPLICATION_NAME";
    public static final String APPLICATION_NAME = getRequiredProperty(APPLICATION_NAME_PROPERTY);

    @Override
    public Sone getSone() {
        return SBS;
    }

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
        FilterRegistration.Dynamic docratorfilter = servletContext.addFilter("docratorfilter", getDecoratorFilter());
        docratorfilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.FORWARD), false, "/index.html");

        ServletRegistration.Dynamic reactapp = servletContext.addServlet("reactapp", new ApplicationServlet());
        reactapp.addMapping("/*");
    }


    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {

    }
}