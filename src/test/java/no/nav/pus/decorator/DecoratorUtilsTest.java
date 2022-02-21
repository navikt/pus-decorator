package no.nav.pus.decorator;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DecoratorUtilsTest {

    @Test
    public void getFragmentPathIfSet() {
        System.setProperty("NAV_DEKORATOREN_URL", "test");
        System.setProperty("NEW_DECORATOR_FRAGMENT_URL", "");

        assertThat(DecoratorUtils.getFragmentPath()).isEqualTo("");
    }

    @Test
    public void getDefaultFragmentPathIfNotSet() {
        System.setProperty("NAV_DEKORATOREN_URL", "test");

        assertThat(DecoratorUtils.getFragmentPath()).isEqualTo("dekoratoren/");
    }
}
