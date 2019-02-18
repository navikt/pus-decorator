package no.nav.pus.decorator.feature;

import lombok.extern.slf4j.Slf4j;
import no.finn.unleash.UnleashContext;
import no.nav.pus.decorator.EnvironmentScriptGenerator;
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
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.*;

import static java.util.stream.Collectors.toMap;
import static no.nav.brukerdialog.security.Constants.ID_TOKEN_COOKIE_NAME;
import static no.nav.brukerdialog.security.oidc.provider.AzureADB2CProvider.AZUREADB2C_OIDC_COOKIE_NAME;

@Path("/")
@Component
@Slf4j
public class FeatureResource {

    private static final String UNLEASH_SESSION_ID_COOKIE_NAME = "UNLEASH_SESSION_ID";
    private static final JwtConsumer JWT_PARSER = new JwtConsumerBuilder()
            .setSkipAllValidators()
            .setSkipSignatureVerification()
            .build();

    private final UnleashService unleashService;

    @Inject
    public FeatureResource(UnleashService unleashService) {
        this.unleashService = unleashService;
    }

    @Path("/")
    public FeatureEvaluator evaluate(
            @QueryParam("feature") List<String> features,
            @CookieParam(UNLEASH_SESSION_ID_COOKIE_NAME) String sessionId,
            @CookieParam(AZUREADB2C_OIDC_COOKIE_NAME) String azureAdB2cOidcToken,
            @CookieParam(ID_TOKEN_COOKIE_NAME) String issoOidcToken,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        return new FeatureEvaluator(features, sessionId, azureAdB2cOidcToken, issoOidcToken, request, response);
    }

    public class FeatureEvaluator {
        private final Map<String, Boolean> evaluation;

        FeatureEvaluator(List<String> features,
                         String sessionId,
                         String azureAdB2cOidcToken,
                         String issoOidcToken,
                         HttpServletRequest request,
                         HttpServletResponse response) {
            UnleashContext unleashContext = UnleashContext.builder()
                    .userId(userIdFromJwt(azureAdB2cOidcToken, issoOidcToken).orElse(null))
                    .sessionId(StringUtils.of(sessionId).orElseGet(() -> generateSessionId(response)))
                    .remoteAddress(request.getRemoteAddr())
                    .build();
            evaluation = features
                    .stream()
                    .collect(toMap(
                            e -> e,
                            e -> unleashService.isEnabled(e, unleashContext),
                            (a, b) -> a
                    ));
        }

        @GET
        @Path("/feature")
        public Map<String, Boolean> getFeatures() {
            return evaluation;
        }

        @GET
        @Path("/feature.js")
        @Produces("application/javascript")
        public String getFeaturesAsJs() {
            return new EnvironmentScriptGenerator().formatMapAsJs(evaluation);
        }

    }


    static Optional<String> userIdFromJwt(String... jwts) {
        return Arrays.stream(jwts)
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
