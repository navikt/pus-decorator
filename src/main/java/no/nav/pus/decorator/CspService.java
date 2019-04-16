package no.nav.pus.decorator;

import no.nav.sbl.util.EnvironmentUtils;

import java.util.function.Function;

import static no.nav.sbl.util.EnvironmentUtils.EnviromentClass.Q;
import static no.nav.sbl.util.EnvironmentUtils.EnviromentClass.T;

public class CspService {

    public static String generateCspDirectives() {
        return ""
                + " default-src 'self' appres.nav.no" + appresTest() + " tjenester.nav.no" + tjenesterTest() + ";"
                + " script-src 'self' 'unsafe-inline' 'unsafe-eval' appres.nav.no" + appresTest() + " www.googletagmanager.com www.google-analytics.com *.psplugin.com *.hotjar.com *.dev-sbs.nais.io;"
                + " img-src 'self' appres.nav.no" + appresTest() + " static.hotjar.com www.google-analytics.com data: ;"
                + " style-src 'self' 'unsafe-inline' appres.nav.no" + appresTest() + " *.dev-sbs.nais.io;"
                + " font-src 'self' *.psplugin.com *.vergic.com static.hotjar.com data: ;"
                + " connect-src 'self' *.psplugin.com in.hotjar.com www.google-analytics.com;"
                + " frame-src vars.hotjar.com video.qbrick.com;" // video i aktivitetsplan, mulig den bør ha spesialisert CSP
                + " report-uri /frontendlogger/api/warn;";
    }

    private static String appresTest() {
        return testResourceDirective(e -> String.format("appres-%s.nav.no", e));
    }

    private static String tjenesterTest() {
        return testResourceDirective(e -> String.format("tjenester-%s.nav.no", e));
    }

    private static String testResourceDirective(Function<String, String> formatter) {
        EnvironmentUtils.EnviromentClass environmentClass = EnvironmentUtils.getEnvironmentClass();
        if (environmentClass == T || environmentClass == Q) {
            return " " + formatter.apply(EnvironmentUtils.requireEnvironmentName());
        } else {
            return "";
        }
    }

}
