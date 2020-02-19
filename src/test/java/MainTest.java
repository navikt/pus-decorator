import no.nav.fasit.FasitUtils;
import no.nav.fasit.ServiceUser;
import no.nav.pus.decorator.spa.SPAConfigResolverTest;
import no.nav.testconfig.ApiAppTest;

import static no.nav.apiapp.feil.FeilMapper.VIS_DETALJER_VED_FEIL;
import static no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig.EXTERNAL_USERS_AZUREAD_B2C_DISCOVERY_URL;
import static no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig.EXTERNAL_USERS_AZUREAD_B2C_EXPECTED_AUDIENCE;
import static no.nav.pus.decorator.ApplicationConfig.APPLICATION_NAME_PROPERTY;
import static no.nav.pus.decorator.DecoratorUtils.APPRES_CMS_URL_PROPERTY;
import static no.nav.pus.decorator.DecoratorUtils.NEW_DECORATOR_URL_PROPERTY;
import static no.nav.pus.decorator.EnvironmentScriptGenerator.PUBLIC_PREFIX;
import static no.nav.pus.decorator.spa.SPAConfigResolver.WEBROOT_PATH_PROPERTY_NAME;
import static no.nav.sbl.util.EnvironmentUtils.Type.PUBLIC;
import static no.nav.sbl.util.EnvironmentUtils.Type.SECRET;
import static no.nav.sbl.util.EnvironmentUtils.setProperty;

public class MainTest {

    public static void main(String... args) throws Exception {
        String applicationName = "decorator";
        ApiAppTest.setupTestContext(ApiAppTest.Config.builder().applicationName(applicationName).build());
        setProperty(VIS_DETALJER_VED_FEIL, Boolean.TRUE.toString(), PUBLIC);
        setProperty(APPLICATION_NAME_PROPERTY, applicationName, PUBLIC);

        /* Ny dekoratør: */
        setProperty(NEW_DECORATOR_URL_PROPERTY, "https://www-q0.nav.no", PUBLIC);
        /* Gammel dekoratør: */
        // setProperty(APPRES_CMS_URL_PROPERTY, "https://appres.nav.no", PUBLIC);

        setProperty(PUBLIC_PREFIX + "prop", "content", PUBLIC);
        setProperty(PUBLIC_PREFIX + "prop1", "content1", PUBLIC);
        setProperty(PUBLIC_PREFIX + "prop2", "content2", PUBLIC);

        setProperty(WEBROOT_PATH_PROPERTY_NAME, SPAConfigResolverTest.getWebappSourceDirectory(), PUBLIC);

        ServiceUser azureADClientId;
        if (FasitUtils.usingMock()) {
            azureADClientId = FasitUtils.getServiceUser("aad_b2c_clientid", "dev-proxy");
        } else {
            azureADClientId = FasitUtils.getServiceUser("aad_b2c_clientid", "veilarbdemo");
        }
        setProperty(EXTERNAL_USERS_AZUREAD_B2C_EXPECTED_AUDIENCE, azureADClientId.username, SECRET);
        setProperty(EXTERNAL_USERS_AZUREAD_B2C_DISCOVERY_URL, FasitUtils.getBaseUrl("aad_b2c_discovery"), PUBLIC);

        Main.main("8765", "8766");
    }

}
