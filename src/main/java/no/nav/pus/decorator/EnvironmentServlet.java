package no.nav.pus.decorator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static no.nav.pus.decorator.ApplicationConfig.APPLICATION_NAME;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;


public class EnvironmentServlet extends HttpServlet {

    public static final String PUBLIC_PREFIX = "PUBLIC_";
    private static final String PUBLIC_PREFIX_PATTERN = "^" + PUBLIC_PREFIX + ".+";

    public static final String ENVIRONMENT_CONTEXT_PROPERTY_NAME = "ENVIRONMENT_CONTEXT";

    private String environmentContext = getOptionalProperty(ENVIRONMENT_CONTEXT_PROPERTY_NAME).orElseGet(() -> APPLICATION_NAME);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String envWrapper = resolvePublicEnvironment();

        resp.setContentType("application/javascript");
        resp.getWriter().write(envWrapper);
    }

    String resolvePublicEnvironment() {
        String env = System.getProperties()
                .stringPropertyNames()
                .stream()
                .filter(name -> name.matches(PUBLIC_PREFIX_PATTERN))
                .reduce("", this::toJs);

        return environmentContext + "={};\n" + env;
    }

    private String toJs(String accumulator, String nextString) {
        return accumulator + String.format("%s.%s='%s';\n",
                environmentContext,
                removePublicPrefix(nextString),
                System.getProperty(nextString));
    }

    private static String removePublicPrefix(String string) {
        return string.replaceFirst(PUBLIC_PREFIX,"");
    }

}
