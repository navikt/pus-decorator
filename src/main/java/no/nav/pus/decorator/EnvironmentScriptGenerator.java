package no.nav.pus.decorator;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static no.nav.pus.decorator.ApplicationConfig.resolveApplicationName;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

public class EnvironmentScriptGenerator {
    public static final String PUBLIC_PREFIX = "PUBLIC_";
    public static final String ENVIRONMENT_CONTEXT_PROPERTY_NAME = "ENVIRONMENT_CONTEXT";
    private static final String PUBLIC_PREFIX_PATTERN = "^" + PUBLIC_PREFIX + ".+";

    private final String environmentContext = getOptionalProperty(ENVIRONMENT_CONTEXT_PROPERTY_NAME).orElseGet(ApplicationConfig::resolveApplicationName);

    public String generate() {
        String values = getEnvironmentVariablesAndSystemProperties()
                .entrySet()
                .stream()
                .filter(prop -> prop.getKey().matches(PUBLIC_PREFIX_PATTERN))
                .map(this::toJs)
                .collect(Collectors.joining(""));

        return environmentContext + "={};\n" + values;
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

    private String removePublicPrefix(String string) {
        return string.replaceFirst(PUBLIC_PREFIX, "");
    }
}
