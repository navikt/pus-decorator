package no.nav.pus.decorator.login;

import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.brukerdialog.security.jaspic.OidcAuthModule;
import no.nav.common.auth.SsoToken;
import no.nav.common.auth.Subject;
import no.nav.pus.decorator.TestUtils;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.of;
import static no.nav.common.auth.SsoToken.oidcToken;
import static no.nav.pus.decorator.login.OidcLoginService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.*;

public class OidcLoginServiceTest {

    private static final int EXPIRATION_TIME = Integer.MAX_VALUE;

    private OidcAuthModule oidcAuthModule = mock(OidcAuthModule.class);
    private AuthConfig authConfig = new AuthConfig().setLoginUrl(TestUtils.url("https://login.nav.no/oidc")).setEnforce(true);
    private OidcLoginService oidcLoginService = newOidcLoginService(authConfig, "/contextpath");
    private HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    private HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    private String requestUrl = "http://app.nav.no/contextpath";
    private String queryString = "a=b&c=d";
    private String completeRequestUrl = "http://app.nav.no/contextpath?a=b&c=d";
    private String requestUrlEncoded = "http%3A%2F%2Fapp.nav.no%2Fcontextpath";
    private String queryStringEncoded = "a%3Db%26c%3Dd";
    private String completeRequestUrlEncoded = "http%3A%2F%2Fapp.nav.no%2Fcontextpath%3Fa%3Db%26c%3Dd";

    @Before
    public void setup() {
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
        when(httpServletRequest.getQueryString()).thenReturn(queryString);
    }

    @Test
    public void getLoginRedirectUrl() {
        assertThat(oidcLoginService.getLoginRedirectUrl(httpServletRequest, httpServletResponse)).hasValue("https://login.nav.no/oidc" +
                "?url=http://app.nav.no/contextpath/api/auth/login" + // url: veilarbstepup
                "&redirect=http://app.nav.no/contextpath/api/auth/login" + // redirect: loginservice
                "&force=true" // force=true: force stepup in veilarbstepup
        );

        Cookie destinationCookie = new Cookie(DESTINATION_COOKIE_NAME, completeRequestUrlEncoded);
        destinationCookie.setPath("/");
        verify(httpServletResponse).addCookie(refEq(destinationCookie));

        authenticateUser();
        assertThat(oidcLoginService.getLoginRedirectUrl(httpServletRequest, httpServletResponse)).isEmpty();
    }

    @Test
    public void getLoginRedirectUr__notEnforced() {
        OidcLoginService notEnforcingOidcLoginService = newOidcLoginService(authConfig.setEnforce(false), null);
        assertThat(notEnforcingOidcLoginService.getLoginRedirectUrl(httpServletRequest, httpServletResponse)).isEmpty();
    }

    @Test
    public void getStatus() {
        assertThat(oidcLoginService.getStatus(httpServletRequest, httpServletResponse)).isEqualTo(new AuthenticationStatusDTO());
        authenticateUser();

        long expirationTimeMillis = EXPIRATION_TIME * 1000L;
        AuthenticationStatusDTO status = oidcLoginService.getStatus(httpServletRequest, httpServletResponse);
        assertThat(status.remainingSeconds).isGreaterThan(1000);
        assertThat(status.setRemainingSeconds(0)) // ignore remainingSeconds as it is relative
                .isEqualTo(new AuthenticationStatusDTO().setExpirationTime(new Date(expirationTimeMillis))
        );
    }

    @Test
    public void postLoginRedirectUrl() {
        assertThat(oidcLoginService.getDestinationUrl(httpServletRequest, httpServletResponse)).isEmpty();

        when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{new Cookie(DESTINATION_COOKIE_NAME, completeRequestUrlEncoded)});
        assertThat(oidcLoginService.getDestinationUrl(httpServletRequest, httpServletResponse)).isEqualTo(of(completeRequestUrl));

        Cookie expiredDestinationCookie = new Cookie(DESTINATION_COOKIE_NAME, completeRequestUrlEncoded);
        expiredDestinationCookie.setMaxAge(0);
        verify(httpServletResponse).addCookie(refEq(expiredDestinationCookie));
    }

    @Test
    public void encode_decode() {
        String example = "http://example.com/a/b/c";
        String encoded = OidcLoginService.encode(example);
        String decoded = OidcLoginService.decode(encoded);
        assertThat(decoded).isEqualTo(example);

        assertThat(encode(requestUrl)).isEqualTo(requestUrlEncoded);
        assertThat(encode(queryString)).isEqualTo(queryStringEncoded);
        assertThat(encode(completeRequestUrl)).isEqualTo(completeRequestUrlEncoded);
    }

    private void authenticateUser() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(EXPIRATION_TIME_ATTRIBUTE_NAME, EXPIRATION_TIME);
        SsoToken oidcToken = oidcToken("token", attributes);
        Subject subject = new Subject("uid", IdentType.EksternBruker, oidcToken);
        when(oidcAuthModule.authenticate(httpServletRequest, httpServletResponse)).thenReturn(of(subject));
    }


    private OidcLoginService newOidcLoginService(AuthConfig authConfig, String contextPath) {
        return new OidcLoginService(authConfig, oidcAuthModule, contextPath);
    }

}