package no.nav.pus.decorator;


import no.nav.sbl.dialogarena.test.junit.SystemPropertiesRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationServiceTest {

    private static final List<ConfigurationService.Feature> FEATURES = Arrays.asList(ConfigurationService.Feature.values());
    private static final ConfigurationService.Feature FEATURE_A = FEATURES.get(0);
    private static final ConfigurationService.Feature FEATURE_B = FEATURES.get(1);

    @Rule
    public SystemPropertiesRule systemPropertiesRule = new SystemPropertiesRule();

    @Test
    public void featuresEnabledByDefault() {
        FEATURES.forEach(feature -> {
            assertThat(ConfigurationService.isEnabled(feature)).isTrue();
            assertThat(ConfigurationService.isDisabled(feature)).isFalse();
        });
    }
    
    @Test
    public void featureCanBeIndividuallyDisabled() {
        systemPropertiesRule.setProperty(FEATURE_A.propertyName, "true");

        assertThat(ConfigurationService.isEnabled(FEATURE_A)).isFalse();
        assertThat(ConfigurationService.isDisabled(FEATURE_A)).isTrue();

        assertThat(ConfigurationService.isEnabled(FEATURE_B)).isTrue();
        assertThat(ConfigurationService.isDisabled(FEATURE_B)).isFalse();
    }

}