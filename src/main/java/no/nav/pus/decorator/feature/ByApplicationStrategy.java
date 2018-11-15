package no.nav.pus.decorator.feature;

import no.finn.unleash.strategy.Strategy;
import no.nav.sbl.util.EnvironmentUtils;

import java.util.Arrays;
import java.util.Map;

import static java.util.Optional.ofNullable;

public class ByApplicationStrategy implements Strategy {

    static final String APP_PARAMETER = "app";

    @Override
    public String getName() {
        return "byApplication";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return ofNullable(parameters)
                .map(p -> p.get(APP_PARAMETER))
                .map(appParameter -> appParameter.split(","))
                .map(this::match)
                .orElse(false);
    }

    private boolean match(String[] activeApplications) {
        return EnvironmentUtils.getApplicationName()
                .map(applicationName -> Arrays.stream(activeApplications).anyMatch(applicationName::equals))
                .orElse(false);
    }

}
