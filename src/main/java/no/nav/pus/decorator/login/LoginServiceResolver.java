package no.nav.pus.decorator.login;

import no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig;
import no.nav.common.oidc.OidcTokenValidator;
import no.nav.pus.decorator.config.Config;

import static java.util.Optional.ofNullable;

public class LoginServiceResolver {

    public static LoginService resolveLoginService(Config config, String contextPath) {
        return ofNullable(config.auth)
                .map(authConfig -> oidcLoginService(authConfig, contextPath))
                .orElse(new NoLoginService());
    }

    private static LoginService oidcLoginService(AuthConfig authConfig, String contextPath) {
        AzureADB2CConfig azureADB2CConfig = AzureADB2CConfig.configureAzureAdForExternalUsers();

        OidcTokenValidator validator = new OidcTokenValidator(azureADB2CConfig.discoveryUrl, azureADB2CConfig.expectedAudience);
        return new OidcLoginService(authConfig, validator, contextPath);
    }

}
