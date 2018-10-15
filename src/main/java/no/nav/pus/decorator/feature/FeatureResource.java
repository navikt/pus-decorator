package no.nav.pus.decorator.feature;

import lombok.extern.slf4j.Slf4j;
import no.finn.unleash.UnleashContext;
import no.nav.sbl.featuretoggle.unleash.UnleashService;
import no.nav.sbl.util.StringUtils;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static no.nav.brukerdialog.security.Constants.ID_TOKEN_COOKIE_NAME;
import static no.nav.brukerdialog.security.oidc.provider.AzureADB2CProvider.AZUREADB2C_OIDC_COOKIE_NAME;

@Path("/feature")
@Component
@Slf4j
public class FeatureResource {

    private static final String QUERY_PARAM_PROPERTY_NAME = "queryParam";
    private static final String QUERY_VALUES_PROPERTY_NAME = "queryValues";
    private static final String UNLEASH_SESSION_ID_COOKIE_NAME = "UNLEASH_SESSION_ID";
    private static final JwtConsumer JWT_PARSER = new JwtConsumerBuilder()
            .setSkipAllValidators()
            .setSkipSignatureVerification()
            .build();
    public static final String FEATURE_QUERY_PARAM_NAME = "feature";

    private final UnleashService unleashService;

    @Inject
    public FeatureResource(UnleashService unleashService) {
        this.unleashService = unleashService;
    }

    @GET
    public Map<String, Boolean> getFeatures(
            @QueryParam("feature") List<String> features,
            @CookieParam(UNLEASH_SESSION_ID_COOKIE_NAME) String sessionId,
            @CookieParam(AZUREADB2C_OIDC_COOKIE_NAME) String azureAdB2cOidcToken,
            @CookieParam(ID_TOKEN_COOKIE_NAME) String issoOidcToken,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        UnleashContext.Builder unleashContextBuilder = UnleashContext.builder()
                .userId(userIdFromJwt(azureAdB2cOidcToken, issoOidcToken).orElse(null))
                .sessionId(StringUtils.of(sessionId).orElseGet(() -> generateSessionId(response)))
                .remoteAddress(request.getRemoteAddr());

        request.getParameterMap().forEach((queryParamName, queryParamValues) -> {
            if (!queryParamName.equals(FEATURE_QUERY_PARAM_NAME)) {
                unleashContextBuilder.addProperty(QUERY_PARAM_PROPERTY_NAME, queryParamName);
                stream(queryParamValues).forEach(value -> unleashContextBuilder.addProperty(QUERY_VALUES_PROPERTY_NAME, value));
            }
        });

        return features.stream().collect(Collectors.toMap(e -> e, e -> unleashService.isEnabled(e, unleashContextBuilder.build())));
    }

    static Optional<String> userIdFromJwt(String... jwts) {
        return stream(jwts)
                .filter(StringUtils::notNullOrEmpty)
                .map(FeatureResource::getSubject)
                .filter(StringUtils::notNullOrEmpty)
                .findAny();
    }

    private static String getSubject(String jwt) {
        try {
            return JWT_PARSER.processToClaims(jwt).getSubject();
        } catch (MalformedClaimException | InvalidJwtException e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    private String generateSessionId(HttpServletResponse httpServletRequest) {
        UUID uuid = UUID.randomUUID();
        String sessionId = Long.toHexString(uuid.getMostSignificantBits()) + Long.toHexString(uuid.getLeastSignificantBits());
        Cookie cookie = new Cookie(UNLEASH_SESSION_ID_COOKIE_NAME, sessionId);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        httpServletRequest.addCookie(cookie);
        return sessionId;
    }

}
