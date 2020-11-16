package no.nav.pus.decorator.login;

import no.nav.common.oidc.OidcTokenValidator;
import no.nav.pus.decorator.config.Config;
import no.nav.sbl.util.EnvironmentUtils;

import static java.util.Optional.ofNullable;

public class LoginServiceResolver {

    public static LoginService resolveLoginService(Config config, String contextPath) {
        return ofNullable(config.auth)
                .map(authConfig -> oidcLoginService(authConfig, contextPath))
                .orElse(new NoLoginService());
    }

    private static LoginService oidcLoginService(AuthConfig authConfig, String contextPath) {
        String discoveryUrl = EnvironmentUtils.getRequiredProperty("LOGINSERVICE_IDPORTEN_DISCOVERY_URL", "AAD_B2C_DISCOVERY_URL");
        String expectedAudience = EnvironmentUtils.getRequiredProperty("LOGINSERVICE_IDPORTEN_AUDIENCE", "AAD_B2C_CLIENTID_USERNAME");

        OidcTokenValidator validator = new OidcTokenValidator(discoveryUrl, expectedAudience);
        return new OidcLoginService(authConfig, validator, contextPath);
    }

}
