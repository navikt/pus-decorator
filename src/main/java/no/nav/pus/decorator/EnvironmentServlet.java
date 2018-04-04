package no.nav.pus.decorator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static no.nav.pus.decorator.ApplicationConfig.APPLICATION_NAME;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;


public class EnvironmentServlet extends HttpServlet {

    public static final String PUBLIC_PREFIX = "PUBLIC_";
    public static final String ENVIRONMENT_CONTEXT_PROPERTY_NAME = "ENVIRONMENT_CONTEXT";
    private static final String PUBLIC_PREFIX_PATTERN = "^" + PUBLIC_PREFIX + ".+";

    private final String environmentContext = getOptionalProperty(ENVIRONMENT_CONTEXT_PROPERTY_NAME).orElseGet(() -> APPLICATION_NAME);

    private static String removePublicPrefix(String string) {
        return string.replaceFirst(PUBLIC_PREFIX, "");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String envWrapper = resolvePublicEnvironment();

        resp.setContentType("application/javascript");
        resp.getWriter().write(envWrapper);
    }

    String resolvePublicEnvironment() {
        StringBuilder sb = new StringBuilder();

        getEnvironmentVariablesAndSystemProperties()
                .entrySet()
                .stream()
                .filter(prop -> prop.getKey().matches(PUBLIC_PREFIX_PATTERN))
                .map(this::toJs)
                .forEach(sb::append);

        return environmentContext + "={};\n" + sb;
    }

    private Map<String, String> getEnvironmentVariablesAndSystemProperties() {
        Map<String, String> map = new HashMap<>(System.getenv());
        System.getProperties().stringPropertyNames().forEach(n -> map.put(n, System.getProperty(n)));
        return map;
    }

    private String toJs(Map.Entry<String, String> prop) {
        return String.format("%s.%s='%s';\n",
                environmentContext,
                removePublicPrefix(prop.getKey()),
                prop.getValue()
        );
    }

}
