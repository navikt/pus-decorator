package no.nav.pus.decorator;

import org.junit.Test;

import static no.nav.pus.decorator.HeaderType.*;
import static org.assertj.core.api.Assertions.assertThat;

public class HeaderTypeTest {

    @Test
    public void getFragmentName(){
        assertThat(WITH_MENU.getFragmentName()).hasValue("header-withmenu");
        assertThat(WITHOUT_MENU.getFragmentName()).hasValue("header");
    }

    @Test
    public void getFragment(){
        assertThat(WITH_MENU.getFragment()).hasValue("{{fragment.header-withmenu}}");
        assertThat(WITHOUT_MENU.getFragment()).hasValue("{{fragment.header}}");
    }

}