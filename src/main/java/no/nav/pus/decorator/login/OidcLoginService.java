package no.nav.pus.decorator.login;

import no.nav.brukerdialog.security.jaspic.OidcAuthModule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class OidcLoginService implements LoginService {
    private final String oidcLoginUrl;
    private final OidcAuthModule oidcAuthModule;

    public OidcLoginService(String oidcLoginUrl, OidcAuthModule oidcAuthModule) {
        this.oidcLoginUrl = oidcLoginUrl;
        this.oidcAuthModule = oidcAuthModule;
    }

    @Override
    public Optional<String> getRedirectUrl(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return oidcAuthModule.authenticate(httpServletRequest, httpServletResponse).isPresent() ? empty() : of(oidcLoginUrl + "?url=" + httpServletRequest.getRequestURL());
    }

}
