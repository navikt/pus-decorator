import no.nav.testconfig.ApiAppTest;

import static no.nav.apiapp.feil.FeilMapper.VIS_DETALJER_VED_FEIL;
import static no.nav.pus.decorator.ApplicationConfig.APPLICATION_NAME_PROPERTY;
import static no.nav.pus.decorator.DecoratorUtils.APPRES_CMS_URL_PROPERTY;
import static no.nav.pus.decorator.EnvironmentScriptGenerator.PUBLIC_PREFIX;
import static no.nav.sbl.util.EnvironmentUtils.Type.PUBLIC;
import static no.nav.sbl.util.EnvironmentUtils.setProperty;

public class MainTest {

    public static final String TEST_PORT = "8765";

    public static void main(String... args) throws Exception {
        ApiAppTest.setupTestContext();
        setProperty(VIS_DETALJER_VED_FEIL, Boolean.TRUE.toString(), PUBLIC);
        setProperty(APPLICATION_NAME_PROPERTY, "decorator", PUBLIC);
        setProperty(APPRES_CMS_URL_PROPERTY, "https://appres.nav.no", PUBLIC);
        setProperty(PUBLIC_PREFIX + "prop", "content", PUBLIC);
        setProperty(PUBLIC_PREFIX + "prop1", "content1", PUBLIC);
        setProperty(PUBLIC_PREFIX + "prop2", "content2", PUBLIC);

        Main.main(TEST_PORT);
    }

}
