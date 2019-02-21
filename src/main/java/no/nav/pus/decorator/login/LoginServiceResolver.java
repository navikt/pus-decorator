package no.nav.pus.decorator.login;

import no.nav.brukerdialog.security.jaspic.OidcAuthModule;
import no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig;
import no.nav.brukerdialog.security.oidc.provider.AzureADB2CProvider;
import no.nav.pus.decorator.config.Config;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

public class LoginServiceResolver {

    public static LoginService resolveLoginService(Config config, String contextPath) {
        return ofNullable(config.auth)
                .map(authConfig -> oidcLoginService(authConfig, contextPath))
                .orElse(new NoLoginService());
    }

    private static LoginService oidcLoginService(AuthConfig authConfig, String contextPath) {
        AzureADB2CConfig azureADB2CConfig = AzureADB2CConfig.readFromSystemProperties();
        AzureADB2CProvider azureADB2CProvider = new AzureADB2CProvider(azureADB2CConfig);
        OidcAuthModule oidcAuthModule = new OidcAuthModule(singletonList(azureADB2CProvider));
        return new OidcLoginService(authConfig, oidcAuthModule, contextPath);
    }

}
