package no.nav.pus.decorator.security;

import no.nav.common.auth.SecurityLevel;
import no.nav.common.auth.SsoToken;
import no.nav.common.auth.Subject;
import no.nav.pus.decorator.login.OidcLoginService;

public class SecurityLevelAuthorizationModule {
    public static boolean authorized(Subject subject, Integer minimumLevel) {
        return subject != null && getSecurityLevel(subject).getSecurityLevel() >= minimumLevel;
    }

    public static SecurityLevel getSecurityLevel(Subject subject) {
        SsoToken ssoToken = subject.getSsoToken();
        switch (ssoToken.getType()) {
            case OIDC:
                return OidcLoginService.getOidcSecurityLevel(ssoToken);
            default:
                return SecurityLevel.Ukjent;
        }
    }

}