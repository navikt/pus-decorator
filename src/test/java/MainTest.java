import no.nav.fasit.FasitUtils;
import no.nav.fasit.ServiceUser;
import no.nav.pus.decorator.spa.SPAConfigResolverTest;
import no.nav.testconfig.ApiAppTest;

import static no.nav.apiapp.feil.FeilMapper.VIS_DETALJER_VED_FEIL;
import static no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig.AZUREAD_B2C_DISCOVERY_URL_PROPERTY_NAME;
import static no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig.AZUREAD_B2C_EXPECTED_AUDIENCE_PROPERTY_NAME;
import static no.nav.pus.decorator.ApplicationConfig.APPLICATION_NAME_PROPERTY;
import static no.nav.pus.decorator.ApplicationConfig.OIDC_LOGIN_URL_PROPERTY_NAME;
import static no.nav.pus.decorator.DecoratorUtils.APPRES_CMS_URL_PROPERTY;
import static no.nav.pus.decorator.EnvironmentScriptGenerator.PUBLIC_PREFIX;
import static no.nav.pus.decorator.spa.SPAConfigResolver.WEBROOT_PATH_PROPERTY_NAME;
import static no.nav.sbl.util.EnvironmentUtils.Type.PUBLIC;
import static no.nav.sbl.util.EnvironmentUtils.Type.SECRET;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;
import static no.nav.sbl.util.EnvironmentUtils.setProperty;

public class MainTest {

    public static void main(String... args) throws Exception {
        String applicationName = "decorator";
        ApiAppTest.setupTestContext(ApiAppTest.Config.builder().applicationName(applicationName).build());
        setProperty(VIS_DETALJER_VED_FEIL, Boolean.TRUE.toString(), PUBLIC);
        setProperty(APPLICATION_NAME_PROPERTY, applicationName, PUBLIC);
        setProperty(APPRES_CMS_URL_PROPERTY, "https://appres.nav.no", PUBLIC);
        setProperty(PUBLIC_PREFIX + "prop", "content", PUBLIC);
        setProperty(PUBLIC_PREFIX + "prop1", "content1", PUBLIC);
        setProperty(PUBLIC_PREFIX + "prop2", "content2", PUBLIC);

        setProperty(WEBROOT_PATH_PROPERTY_NAME, SPAConfigResolverTest.getWebappSourceDirectory(), PUBLIC);

        if (getOptionalProperty(OIDC_LOGIN_URL_PROPERTY_NAME).isPresent()) {
            ServiceUser azureADClientId;
            if (FasitUtils.usingMock()) {
                azureADClientId = FasitUtils.getServiceUser("aad_b2c_clientid", "dev-proxy");
                setProperty(OIDC_LOGIN_URL_PROPERTY_NAME,"http://localhost:8080/mock/azureadb2c/authorize", PUBLIC);
            } else {
                azureADClientId = FasitUtils.getServiceUser("aad_b2c_clientid", "veilarbdemo");
            }
            setProperty(AZUREAD_B2C_EXPECTED_AUDIENCE_PROPERTY_NAME, azureADClientId.username, SECRET);
            setProperty(AZUREAD_B2C_DISCOVERY_URL_PROPERTY_NAME, FasitUtils.getBaseUrl("aad_b2c_discovery"), PUBLIC);
        }

        Main.main("8765", "8766");
    }

}
