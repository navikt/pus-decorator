package no.nav.pus.decorator.login;

import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig;
import no.nav.common.oidc.OidcTokenValidator;
import no.nav.pus.decorator.config.Config;
import no.nav.sbl.util.EnvironmentUtils;

import static java.util.Optional.ofNullable;
import static no.nav.brukerdialog.security.Constants.AZUREADB2C_OIDC_COOKIE_NAME_SBS;
import static no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig.EXTERNAL_USERS_AZUREAD_B2C_DISCOVERY_URL;
import static no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig.EXTERNAL_USERS_AZUREAD_B2C_EXPECTED_AUDIENCE;

public class LoginServiceResolver {

    public static LoginService resolveLoginService(Config config, String contextPath) {
        return ofNullable(config.auth)
                .map(authConfig -> oidcLoginService(authConfig, contextPath))
                .orElse(new NoLoginService());
    }

    private static LoginService oidcLoginService(AuthConfig authConfig, String contextPath) {
        String discoveryUrl = EnvironmentUtils.getRequiredProperty("LOGINSERVICE_IDPORTEN_DISCOVERY_URL", EXTERNAL_USERS_AZUREAD_B2C_DISCOVERY_URL);
        String expectedAudience = EnvironmentUtils.getRequiredProperty("LOGINSERVICE_IDPORTEN_AUDIENCE", EXTERNAL_USERS_AZUREAD_B2C_EXPECTED_AUDIENCE);

        AzureADB2CConfig azureADB2CConfig = AzureADB2CConfig.builder()
                .discoveryUrl(discoveryUrl)
                .expectedAudience(expectedAudience)
                .tokenName(AZUREADB2C_OIDC_COOKIE_NAME_SBS)
                .identType(IdentType.EksternBruker)
                .build();

        OidcTokenValidator validator = new OidcTokenValidator(azureADB2CConfig.discoveryUrl, azureADB2CConfig.expectedAudience);
        return new OidcLoginService(authConfig, validator, contextPath);
    }

}
