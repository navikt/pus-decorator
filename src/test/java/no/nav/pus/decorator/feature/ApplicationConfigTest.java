package no.nav.pus.decorator.feature;

import no.nav.pus.decorator.ApplicationConfig;
import no.nav.pus.decorator.ConfigurationService;
import no.nav.sbl.featuretoggle.unleash.UnleashService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationConfigTest extends ApplicationConfig {

    @Bean
    @Conditional({ConfigurationService.UnleashEnabled.class})
    @Override
    public UnleashService unleashService(Provider<HttpServletRequest> httpServletRequestProvider) {
        UnleashService u =  mock(UnleashService.class);
        when(u.isEnabled(any())).thenReturn(true);
        return u;
    }
}
