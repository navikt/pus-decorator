package no.nav.pus.decorator;


import org.junit.Test;

import static no.nav.sbl.dialogarena.test.SystemProperties.setTemporaryProperty;
import static no.nav.sbl.util.EnvironmentUtils.FASIT_ENVIRONMENT_NAME_PROPERTY_NAME;
import static org.assertj.core.api.Assertions.assertThat;

public class CspServiceTest {

    @Test
    public void generateCspDirectives_prod() {
        assertThat(CspService.generateCspDirectives()).isEqualTo("" +
                " default-src 'self' appres.nav.no tjenester.nav.no;" +
                " script-src 'self' 'unsafe-inline' 'unsafe-eval' appres.nav.no www.googletagmanager.com www.google-analytics.com static.hotjar.com;" +
                " style-src 'self' 'unsafe-inline' appres.nav.no;" +
                " font-src 'self' data: ;" +
                " report-uri /frontendlogger/api/warn;");
    }

    @Test
    public void generateCspDirectives_test() {
        setTemporaryProperty(FASIT_ENVIRONMENT_NAME_PROPERTY_NAME, "q6", () -> {
            assertThat(CspService.generateCspDirectives()).isEqualTo("" +
                    " default-src 'self' appres.nav.no tjenester.nav.no appres-q6.nav.no tjenester-q6.nav.no;" +
                    " script-src 'self' 'unsafe-inline' 'unsafe-eval' appres.nav.no www.googletagmanager.com www.google-analytics.com static.hotjar.com;" +
                    " style-src 'self' 'unsafe-inline' appres.nav.no;" +
                    " font-src 'self' data: ;" +
                    " report-uri /frontendlogger/api/warn;"
            );
        });
    }

}