package no.nav.pus.decorator.feature;


import no.nav.sbl.util.EnvironmentUtils;
import org.junit.Test;

import java.util.HashMap;

import static no.nav.pus.decorator.feature.ByApplicationStrategy.APP_PARAMETER;
import static no.nav.sbl.dialogarena.test.SystemProperties.setTemporaryProperty;
import static org.assertj.core.api.Assertions.assertThat;

public class ByApplicationStrategyTest {

    private final ByApplicationStrategy byApplicationStrategy = new ByApplicationStrategy();

    @Test
    public void isEnabled__disabled() {
        assertThat(byApplicationStrategy.isEnabled(null)).isFalse();
        assertThat(byApplicationStrategy.isEnabled(app("testapp"))).isFalse();
        assertThat(byApplicationStrategy.isEnabled(app("otherapp,testapp"))).isFalse();
    }

    @Test
    public void isEnabled__parameter_matches_application__enabled() {
        setTemporaryProperty(EnvironmentUtils.APP_NAME_PROPERTY_NAME, "testapp", () -> {
            assertThat(byApplicationStrategy.isEnabled(app("testapp"))).isTrue();
            assertThat(byApplicationStrategy.isEnabled(app("otherapp,testapp"))).isTrue();
        });
    }

    private HashMap<String, String> app(final String app) {
        return new HashMap<String, String>() {{
            put(APP_PARAMETER, app);
        }};
    }

}