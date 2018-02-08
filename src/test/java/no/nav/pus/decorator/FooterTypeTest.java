package no.nav.pus.decorator;

import org.junit.Test;

import static no.nav.pus.decorator.FooterType.NO_FOOTER;
import static no.nav.pus.decorator.FooterType.WITHOUT_ALPHABET;
import static no.nav.pus.decorator.FooterType.WITH_ALPHABET;
import static org.assertj.core.api.Assertions.assertThat;

public class FooterTypeTest {

    @Test
    public void getFragmentName(){
        assertThat(WITH_ALPHABET.getFragmentName()).hasValue("footer-withmenu");
        assertThat(WITHOUT_ALPHABET.getFragmentName()).hasValue("footer");
        assertThat(NO_FOOTER.getFragmentName()).isEmpty();
    }

    @Test
    public void getFragment(){
        assertThat(WITH_ALPHABET.getFragment()).hasValue("{{fragment.footer-withmenu}}");
        assertThat(WITHOUT_ALPHABET.getFragment()).hasValue("{{fragment.footer}}");
        assertThat(NO_FOOTER.getFragment()).isEmpty();
    }

}