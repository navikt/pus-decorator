
import no.nav.apiapp.ApiApp;
import no.nav.pus.decorator.ApplicationConfig;
import no.nav.sbl.util.EnvironmentUtils;

import static no.nav.sbl.dialogarena.common.jetty.ToUrl.JETTY_PRINT_LOCALHOST;
import static no.nav.sbl.util.EnvironmentUtils.Type.PUBLIC;
import static no.nav.sbl.util.EnvironmentUtils.setProperty;

public class Main {

    public static void main(String... args) throws Exception {
        setProperty(JETTY_PRINT_LOCALHOST, "true", PUBLIC);
        ApiApp.runApp(ApplicationConfig.class, args);
    }

}
