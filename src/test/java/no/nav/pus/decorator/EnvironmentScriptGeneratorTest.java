package no.nav.pus.decorator;

import org.junit.Test;

import static no.nav.pus.decorator.EnvironmentScriptGenerator.ENVIRONMENT_CONTEXT_PROPERTY_NAME;
import static no.nav.sbl.dialogarena.test.SystemProperties.setTemporaryProperty;
import static org.assertj.core.api.Assertions.assertThat;

public class EnvironmentScriptGeneratorTest {
    @Test
    public void resolvePublicEnvironment__no_public_environment() {
        setTemporaryProperty(ENVIRONMENT_CONTEXT_PROPERTY_NAME, EnvironmentScriptGeneratorTest.class.getSimpleName(), () -> {
            assertThat(new EnvironmentScriptGenerator().generate()).isEqualTo("EnvironmentScriptGeneratorTest={};\n");
        });
    }

    @Test
    public void resolvePublicEnvironment__with_public_environment() {
        setTemporaryProperty("PRIVATE_NOT_INCLUDE", "secret", () -> {
            setTemporaryProperty("PUBLIC_ABC", "abc", () -> {
                setTemporaryProperty("PUBLIC_DEF", "def", () -> {
                    setTemporaryProperty(ENVIRONMENT_CONTEXT_PROPERTY_NAME, EnvironmentScriptGeneratorTest.class.getSimpleName(), () -> {
                        assertThat(new EnvironmentScriptGenerator().generate()).isEqualTo("" +
                                "EnvironmentScriptGeneratorTest={};\n" +
                                "EnvironmentScriptGeneratorTest.DEF='def';\n" +
                                "EnvironmentScriptGeneratorTest.ABC='abc';\n"
                        );
                    });
                });
            });
        });
    }
}
