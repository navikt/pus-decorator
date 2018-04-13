package no.nav.pus.decorator;

import org.junit.Test;

import static no.nav.pus.decorator.HeaderType.*;
import static org.assertj.core.api.Assertions.assertThat;

public class HeaderTypeTest {

    @Test
    public void getFragmentName(){
        assertThat(WITH_MENU.getFragmentName()).hasValue("footer-withmenu");
        assertThat(WITHOUT_MENU.getFragmentName()).hasValue("footer");
        assertThat(NO_HEADER.getFragmentName()).isEmpty();
    }

    @Test
    public void getFragment(){
        assertThat(WITH_MENU.getFragment()).hasValue("{{fragment.header-withmenu}}");
        assertThat(WITHOUT_MENU.getFragment()).hasValue("{{fragment.header}}");
        assertThat(NO_HEADER.getFragment()).isEmpty();
    }

}