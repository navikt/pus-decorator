package no.nav.pus.decorator.login;

import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.brukerdialog.security.jaspic.OidcAuthModule;
import no.nav.common.auth.Subject;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Optional.of;
import static no.nav.common.auth.SsoToken.oidcToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OidcLoginServiceTest {

    private OidcAuthModule oidcAuthModule = mock(OidcAuthModule.class);

    @Test
    public void getRedirectUrl() {
        OidcLoginService oidcLoginService = new OidcLoginService("https://login.nav.no/oidc", oidcAuthModule);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://app.nav.no/contextpath"));
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);

        assertThat(oidcLoginService.getRedirectUrl(httpServletRequest, httpServletResponse)).hasValue("https://login.nav.no/oidc?url=http://app.nav.no/contextpath");

        when(oidcAuthModule.authenticate(httpServletRequest, httpServletResponse)).thenReturn(of(new Subject("uid", IdentType.EksternBruker, oidcToken("token"))));
        assertThat(oidcLoginService.getRedirectUrl(httpServletRequest, httpServletResponse)).isEmpty();
    }

}