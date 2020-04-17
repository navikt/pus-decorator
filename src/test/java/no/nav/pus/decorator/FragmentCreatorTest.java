package no.nav.pus.decorator;

import no.nav.pus.decorator.config.DecoratorConfig;
import org.junit.Test;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;
import static no.nav.pus.decorator.EnvironmentScriptGenerator.ENVIRONMENT_CONTEXT_PROPERTY_NAME;
import static no.nav.pus.decorator.FragmentCreator.readFile;
import static no.nav.sbl.dialogarena.test.SystemProperties.setTemporaryProperty;
import static org.assertj.core.api.Assertions.assertThat;

public class FragmentCreatorTest {

    private DecoratorConfig decoratorConfig = new DecoratorConfig();

    @Test
    public void createFragmentTemplate() {
        setTemporaryProperty(ENVIRONMENT_CONTEXT_PROPERTY_NAME, "testapp", () -> {
            FragmentCreator fragmentCreator = new FragmentCreator(decoratorConfig, "testapp");
            String fragmentTemplate = fragmentCreator.createFragmentTemplate(readFile("/fragmentCreatorTest/original.html"));
            System.out.println(fragmentTemplate);
            assertThat(normalize(fragmentTemplate)).isEqualTo(normalize(readFile("/fragmentCreatorTest/merged.html")));
        });
    }

    @Test
    public void createFragmentTemplateWithEnvironment() {
        setTemporaryProperty(ENVIRONMENT_CONTEXT_PROPERTY_NAME, "testapp", () -> {
            setTemporaryProperty("PUBLIC_MY_PROPERTY", "public_value", () -> {
                FragmentCreator fragmentCreator = new FragmentCreator(decoratorConfig, "testapp");
                String fragmentTemplate = fragmentCreator.createFragmentTemplate(readFile("/fragmentCreatorTest/original.html"));
                System.out.println(fragmentTemplate);
                assertThat(normalize(fragmentTemplate)).isEqualTo(normalize(readFile("/fragmentCreatorTest/mergedWithEnvironment.html")));
            });
        });
    }

    private String normalize(String s) {
        return Arrays.stream(s.split("\n"))
                .map(String::trim)
                .collect(joining("\n"));
    }

}
