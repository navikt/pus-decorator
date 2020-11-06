package no.nav.pus.decorator.login;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import lombok.SneakyThrows;
import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.common.auth.SsoToken;
import no.nav.common.auth.Subject;
import no.nav.common.oidc.OidcTokenValidator;
import no.nav.common.oidc.utils.TokenUtils;
import no.nav.pus.decorator.ApplicationConfig;
import org.jose4j.jwt.ReservedClaimNames;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static no.nav.common.oidc.Constants.AZURE_AD_B2C_ID_TOKEN_COOKIE_NAME;
import static no.nav.sbl.util.AssertUtils.assertNotNull;
import static no.nav.sbl.util.StringUtils.notNullOrEmpty;

public class OidcLoginService implements LoginService {
    private final long minRemainingSeconds;

    private static final String UTF_8 = "UTF-8";
    private static final String SECURITY_LEVEL_ATTRIBUTE = "acr";

    static final String DESTINATION_COOKIE_NAME = "login_dest";
    static final String EXPIRATION_TIME_ATTRIBUTE_NAME = ReservedClaimNames.EXPIRATION_TIME;

    private final URL oidcLoginUrl;
    private final boolean enforce;
    private final OidcTokenValidator validator;
    private final String contextPath;
    private final int minSecurityLevel;

    public OidcLoginService(AuthConfig authConfig, OidcTokenValidator validator, String contextPath) {
        this.enforce = authConfig.enforce;
        this.oidcLoginUrl = authConfig.enforce ? assertNotNull(authConfig.loginUrl, "loginUrl må spesifiseres når enforce=true") : null;
        this.validator = validator;
        this.contextPath = contextPath;
        this.minSecurityLevel = authConfig.minSecurityLevel;
        this.minRemainingSeconds = authConfig.minRemainingSeconds;
    }

    @Override
    public AuthenticationStatusDTO getStatus(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Optional<SsoToken> ssoToken = authenticate(httpServletRequest, httpServletResponse)
                .map(Subject::getSsoToken);

        long expirationTimestamp = ssoToken
                .map(SsoToken::getAttributes)
                .map(a -> a.get(EXPIRATION_TIME_ATTRIBUTE_NAME))
                .map(i -> (Date) i)
                .map(Date::getTime)
                .orElse((long) 0);

        JWTSecurityLevel securityLevelTest = new JWTSecurityLevel(ssoToken);


        long remainingMillis = expirationTimestamp - System.currentTimeMillis();

        return new AuthenticationStatusDTO()
                .setLoggedIn(ssoToken.isPresent())
                .setSecurityLevel(JWTSecurityLevel.getSecurityLevel())
                .setExpirationTime(remainingMillis > 0 ? new Date(expirationTimestamp) : null)
                .setRemainingSeconds(Math.max(remainingMillis / 1000, 0L));
    }

    @Override
    public Optional<String> getLoginRedirectUrl(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (enforce) {

            AuthenticationStatusDTO status = getStatus(httpServletRequest, httpServletResponse);
            int securityLevel = status.getSecurityLevel().getSecurityLevel();

            if (status.remainingSeconds < minRemainingSeconds || securityLevel < minSecurityLevel) {
                Cookie cookie = new Cookie(DESTINATION_COOKIE_NAME, encode(buildDestinationUrl(httpServletRequest)));
                cookie.setPath("/");
                httpServletResponse.addCookie(cookie);
                String returnUrl = buildReturnUrl(httpServletRequest);
                return of(String.format("%s?url=%s&redirect=%s&force=true",
                        oidcLoginUrl,
                        returnUrl,
                        returnUrl
                ));
            }
        }
        return Optional.empty();
    }

    private String buildDestinationUrl(HttpServletRequest httpServletRequest) {
        String requestUrl = httpServletRequest.getRequestURL().toString();
        String queryString = httpServletRequest.getQueryString();
        return notNullOrEmpty(queryString) ? requestUrl + "?" + queryString : requestUrl;
    }

    private String buildReturnUrl(HttpServletRequest httpServletRequest) {
        return UriBuilder.fromUri(httpServletRequest.getRequestURL().toString())
                .replacePath(contextPath)
                .path(ApplicationConfig.DEFAULT_API_PATH)
                .path(AuthenticationResource.AUTHENTICATION_RESOURCE_PATH)
                .path(AuthenticationResource.LOGIN_PATH)
                .build()
                .toString();
    }

    @Override
    public Optional<String> getDestinationUrl(HttpServletRequest httpServletRequest, HttpServletResponse
            httpServletResponse) {
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (DESTINATION_COOKIE_NAME.equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    httpServletResponse.addCookie(cookie);
                    return ofNullable(cookie.getValue()).map(OidcLoginService::decode);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Subject> authenticate(HttpServletRequest httpServletRequest, HttpServletResponse
            httpServletResponse) {

        Optional<String> token = getToken(httpServletRequest);
        try {
            if(!token.isPresent()){
                return Optional.empty();
            }

            JWT jwtToken = JWTParser.parse(token.get());

            validator.validate(jwtToken);

            SsoToken ssoToken = SsoToken.oidcToken(jwtToken.getParsedString(), jwtToken.getJWTClaimsSet().getClaims());
            Subject subject = new Subject(
                    TokenUtils.getUid(jwtToken, IdentType.EksternBruker),
                    IdentType.EksternBruker,
                    ssoToken
            );

            return Optional.of(subject);

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private Optional<String> getToken(HttpServletRequest request) {

        Optional<String> tokenFromCookie = getTokenFromCookie(request, AZURE_AD_B2C_ID_TOKEN_COOKIE_NAME);

        if (tokenFromCookie.isPresent()) {
            return tokenFromCookie;
        }

        return TokenUtils.getTokenFromHeader(request);
    }

    private Optional<String> getTokenFromCookie(HttpServletRequest request, String cookieName) {
        return Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays
                        .stream(cookies)
                        .filter(cookie -> cookie.getName().equals(cookieName) && cookie.getValue() != null)
                        .findFirst()
                        .map(Cookie::getValue)
                );
    }

    @SneakyThrows
    static String encode(String requestURI) {
        return URLEncoder.encode(requestURI, UTF_8);
    }

    @SneakyThrows
    static String decode(String value) {
        return URLDecoder.decode(value, UTF_8);
    }

}
