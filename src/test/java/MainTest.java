import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.testconfig.ApiAppTest;

import static no.nav.pus.decorator.ApplicationConfig.APPLICATION_NAME_PROPERTY;
import static no.nav.pus.decorator.DecoratorUtils.APPRES_CMS_URL_PROPERTY;
import static no.nav.sbl.util.EnvironmentUtils.Type.PUBLIC;
import static no.nav.sbl.util.EnvironmentUtils.setProperty;

public class MainTest {

    public static void main(String... args) throws Exception {
        ApiAppTest.setupTestContext();
        setProperty(APPLICATION_NAME_PROPERTY, "decorator", PUBLIC);
        setProperty(APPRES_CMS_URL_PROPERTY, FasitUtils.getBaseUrl("appres.cms"),PUBLIC);
        Main.main(args);
    }

}
