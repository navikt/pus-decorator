package no.nav.pus.decorator.security;

import no.nav.common.auth.SecurityLevel;
import no.nav.common.auth.Subject;

public class SecurityLevelAuthorizationModule {
    public static boolean authorized(Subject subject, Integer minimumLevel) {
        return subject != null && getSecurityLevel(subject).getSecurityLevel() >= minimumLevel;
    }

    private static SecurityLevel getSecurityLevel(Subject subject) {
        return SecurityLevel.getOidcSecurityLevel(subject.getSsoToken());
    }

}