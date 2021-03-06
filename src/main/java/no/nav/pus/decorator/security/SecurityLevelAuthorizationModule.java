package no.nav.pus.decorator.security;

import no.nav.common.auth.SecurityLevel;
import no.nav.common.auth.Subject;

public class SecurityLevelAuthorizationModule {
    public static boolean authorized(Subject subject, Integer minimumLevel) {
        return subject != null && getOidcSecurityLevel(subject) >= minimumLevel;
    }

    private static Integer getOidcSecurityLevel(Subject subject) {
        return SecurityLevel.getOidcSecurityLevel(subject.getSsoToken()).getSecurityLevel();
    }

}