package no.nav.pus.decorator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;


public class EnvironmentServlet extends HttpServlet {

    public static final String PUBLIC_PREFIX = "PUBLIC_";
    private static final String PUBLIC_PREFIX_PATTERN = "^" + PUBLIC_PREFIX + ".+";
    private static final String ENVIORMENT = getOptionalProperty("ENVIORMENT_PREFIX").orElse("enviorment");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String env = System.getProperties()
                .stringPropertyNames()
                .stream()
                .filter(name -> name.matches(PUBLIC_PREFIX_PATTERN))
                .reduce("", EnvironmentServlet::toJs);

        String envWrapper = ENVIORMENT + "={};\n" + env;

        resp.setContentType("application/javascript");
        resp.getWriter().write(envWrapper);
    }

    private static String toJs(String accumulator, String nextString) {
        return accumulator + String.format("%s.%s='%s';\n",
                ENVIORMENT,
                removePublicPrefix(nextString),
                System.getProperty(nextString));
    }

    private static String removePublicPrefix(String string) {
        return string.replaceFirst(PUBLIC_PREFIX,"");
    }

}
