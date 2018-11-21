package no.nav.pus.decorator;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

public class EnvironmentScriptGenerator {
    public static final String PUBLIC_PREFIX = "PUBLIC_";
    public static final String ENVIRONMENT_CONTEXT_PROPERTY_NAME = "ENVIRONMENT_CONTEXT";
    private static final String PUBLIC_PREFIX_PATTERN = "^" + PUBLIC_PREFIX + ".+";

    private final String environmentContext = getOptionalProperty(ENVIRONMENT_CONTEXT_PROPERTY_NAME).orElseGet(ApplicationConfig::resolveApplicationName);

    public String generate() {
        return formatMapAsJs(getEnvironmentVariablesAndSystemProperties());
    }

    public String formatMapAsJs(Map<String, ?> map) {
        String values = map
                .entrySet()
                .stream()
                .map(this::toJs)
                .collect(Collectors.joining(""));

        return environmentContext + " = window." + environmentContext + " || {};\n" + values;
    }

    private Map<String, String> getEnvironmentVariablesAndSystemProperties() {
        Properties properties = new Properties();
        properties.putAll(System.getenv());
        properties.putAll(System.getProperties());

        return properties
                .stringPropertyNames()
                .stream()
                .filter(prop -> prop.matches(PUBLIC_PREFIX_PATTERN))
                .collect(Collectors.toMap(this::removePublicPrefix, properties::getProperty));
    }

    private String toJs(Map.Entry<String, ?> prop) {
        Object value = prop.getValue();
        return String.format("%s['%s']=%s;\n",
                environmentContext,
                prop.getKey(),
                value instanceof String ? "'" + value + "'" : value
        );
    }

    private String removePublicPrefix(String string) {
        return string.replaceFirst(PUBLIC_PREFIX, "");
    }
}
