package no.nav.pus.decorator;

import org.junit.Test;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;
import static no.nav.pus.decorator.EnvironmentScriptGenerator.ENVIRONMENT_CONTEXT_PROPERTY_NAME;
import static no.nav.pus.decorator.FragmentCreator.readTemplate;
import static no.nav.sbl.dialogarena.test.SystemProperties.setTemporaryProperty;
import static org.assertj.core.api.Assertions.assertThat;

public class FragmentCreatorTest {


    @Test
    public void createFragmentTemplate() {
        setTemporaryProperty(ENVIRONMENT_CONTEXT_PROPERTY_NAME, "testapp", () -> {
            FragmentCreator fragmentCreator = new FragmentCreator("testapp");
            String fragmentTemplate = fragmentCreator.createFragmentTemplate(readTemplate("/fragmentCreatorTest/original.html"));
            System.out.println(fragmentTemplate);
            assertThat(normalize(fragmentTemplate)).isEqualTo(normalize(readTemplate("/fragmentCreatorTest/merged.html")));
        });
    }

    @Test
    public void createFragmentTemplateWithEnvironment() {
        setTemporaryProperty(ENVIRONMENT_CONTEXT_PROPERTY_NAME, "testapp", () -> {
            setTemporaryProperty("PUBLIC_MY_PROPERTY", "public_value", () -> {
                FragmentCreator fragmentCreator = new FragmentCreator("testapp");
                String fragmentTemplate = fragmentCreator.createFragmentTemplate(readTemplate("/fragmentCreatorTest/original.html"));
                System.out.println(fragmentTemplate);
                assertThat(normalize(fragmentTemplate)).isEqualTo(normalize(readTemplate("/fragmentCreatorTest/mergedWithEnvironment.html")));
            });
        });
    }

    private String normalize(String s) {
        return Arrays.stream(s.split("\n"))
                .map(String::trim)
                .collect(joining("\n"));
    }

}
