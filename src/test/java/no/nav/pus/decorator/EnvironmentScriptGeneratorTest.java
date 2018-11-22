package no.nav.pus.decorator;

import org.junit.Test;

import java.util.HashMap;

import static no.nav.pus.decorator.EnvironmentScriptGenerator.ENVIRONMENT_CONTEXT_PROPERTY_NAME;
import static no.nav.sbl.dialogarena.test.SystemProperties.setTemporaryProperty;
import static org.assertj.core.api.Assertions.assertThat;

public class EnvironmentScriptGeneratorTest {

    @Test
    public void generateJsMapFromTypes(){
        setTemporaryProperty(ENVIRONMENT_CONTEXT_PROPERTY_NAME, EnvironmentScriptGeneratorTest.class.getSimpleName(), () -> {
            HashMap<String, Object> map = new HashMap<>();
            map.put("integer_key", 1);
            map.put("boolean_key", false);
            map.put("string_key", "string value");
            assertThat(new EnvironmentScriptGenerator().formatMapAsJs(map)).isEqualTo("" +
                    "EnvironmentScriptGeneratorTest = window.EnvironmentScriptGeneratorTest || {};\n" +
                    "EnvironmentScriptGeneratorTest['string_key']='string value';\n" +
                    "EnvironmentScriptGeneratorTest['integer_key']=1;\n" +
                    "EnvironmentScriptGeneratorTest['boolean_key']=false;\n"
            );
        });
    }

    @Test
    public void resolvePublicEnvironment__no_public_environment() {
        setTemporaryProperty(ENVIRONMENT_CONTEXT_PROPERTY_NAME, EnvironmentScriptGeneratorTest.class.getSimpleName(), () -> {
            assertThat(new EnvironmentScriptGenerator().generate())
                    .isEqualTo("EnvironmentScriptGeneratorTest = window.EnvironmentScriptGeneratorTest || {};\n");
        });
    }

    @Test
    public void resolvePublicEnvironment__with_public_environment() {
        setTemporaryProperty("PRIVATE_NOT_INCLUDE", "secret", () -> {
            setTemporaryProperty("PUBLIC_ABC", "abc", () -> {
                setTemporaryProperty("PUBLIC_DEF", "def", () -> {
                    setTemporaryProperty(ENVIRONMENT_CONTEXT_PROPERTY_NAME, EnvironmentScriptGeneratorTest.class.getSimpleName(), () -> {
                        assertThat(new EnvironmentScriptGenerator().generate()).isEqualTo("" +
                                "EnvironmentScriptGeneratorTest = window.EnvironmentScriptGeneratorTest || {};\n" +
                                "EnvironmentScriptGeneratorTest['ABC']='abc';\n" +
                                "EnvironmentScriptGeneratorTest['DEF']='def';\n"
                        );
                    });
                });
            });
        });
    }
}
