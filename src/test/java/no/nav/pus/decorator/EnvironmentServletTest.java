package no.nav.pus.decorator;

import org.junit.Test;

import static no.nav.pus.decorator.EnvironmentServlet.ENVIRONMENT_CONTEXT_PROPERTY_NAME;
import static no.nav.sbl.dialogarena.test.SystemProperties.setTemporaryProperty;
import static org.assertj.core.api.Assertions.assertThat;

public class EnvironmentServletTest {


    @Test
    public void resolvePublicEnvironment__no_public_environment() {
        setTemporaryProperty(ENVIRONMENT_CONTEXT_PROPERTY_NAME, EnvironmentServletTest.class.getSimpleName(), () -> {
            assertThat(new EnvironmentServlet().resolvePublicEnvironment()).isEqualTo("EnvironmentServletTest={};\n");
        });
    }

    @Test
    public void resolvePublicEnvironment__with_public_environment() {
        setTemporaryProperty("PUBLIC_ABC", "abc", () -> {
            setTemporaryProperty("PUBLIC_DEF", "def", () -> {
                setTemporaryProperty(ENVIRONMENT_CONTEXT_PROPERTY_NAME, EnvironmentServletTest.class.getSimpleName(), () -> {
                    assertThat(new EnvironmentServlet().resolvePublicEnvironment()).isEqualTo("" +
                            "EnvironmentServletTest={};\n" +
                            "EnvironmentServletTest.DEF='def';\n" +
                            "EnvironmentServletTest.ABC='abc';\n"
                    );
                });
            });
        });
    }
}