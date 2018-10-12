package no.nav.pus.decorator;

import no.nav.sbl.util.EnvironmentUtils;

import static no.nav.sbl.util.EnvironmentUtils.EnviromentClass.Q;
import static no.nav.sbl.util.EnvironmentUtils.EnviromentClass.T;

public class CspService {

    public static String generateCspDirectives() {
        return ""
                + " default-src 'self' appres.nav.no tjenester.nav.no" + testResourceDirectives() + ";"
                + " script-src 'self' 'unsafe-inline' 'unsafe-eval' appres.nav.no www.googletagmanager.com www.google-analytics.com static.hotjar.com;"
                + " style-src 'self' 'unsafe-inline' appres.nav.no;"
                + " font-src 'self' data: ;"
                + " report-uri /frontendlogger/api/warn;";
    }

    private static String testResourceDirectives() {
        EnvironmentUtils.EnviromentClass environmentClass = EnvironmentUtils.getEnvironmentClass();
        if (environmentClass == T || environmentClass == Q) {
            String environmentName = EnvironmentUtils.requireEnvironmentName();
            return String.format(" appres-%s.nav.no tjenester-%s.nav.no", environmentName, environmentName);
        } else {
            return "";
        }
    }

}
