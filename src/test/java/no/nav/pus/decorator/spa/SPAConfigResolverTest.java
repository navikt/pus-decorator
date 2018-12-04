package no.nav.pus.decorator.spa;

import org.junit.Test;

import java.io.File;

import static no.nav.pus.decorator.spa.SPAConfigResolver.parseDecoratorConfiguration;
import static no.nav.pus.decorator.spa.SPAConfigResolver.resolveSpaConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

public class SPAConfigResolverTest {

    @Test
    public void resolveSpaConfiguration_default() {
        assertThat(resolveSpaConfiguration())
                .extracting("forwardTarget", "urlPattern")
                .containsExactly(
                        tuple("/index.html", "/*"),
                        tuple("/demo/index.html", "/demo/*"));


    }

    @Test
    public void resolveSpaConfiguration_nonExistingFile() {
        assertThat(resolveSpaConfiguration(new File("/non-existing-file.json")))
                .extracting("forwardTarget", "urlPattern")
                .containsExactly(
                        tuple("/index.html", "/*"),
                        tuple("/demo/index.html", "/demo/*"));
    }

    @Test
    public void parseDecoratorConfiguration_withFile() {
        assertThat(
                parseDecoratorConfiguration(json("/demo-spa.config.json"))).extracting("forwardTarget", "urlPattern")
                .containsExactly(
                        tuple("/app-1.html", "/app1"),
                        tuple("/small/smaller-app.html", "/small/app/*"));
    }

    @Test
    public void resolveSpaConfiguration_fail_if_forward_target_does_not_exist() {
        assertThatThrownBy(() -> resolveSpaConfiguration(json("/demo-spa.config.json")))
                .hasMessageContaining("not found");
    }

    @Test
    public void resolveSpaConfiguration_fail_if_duplicated_url_pattern() {
        assertThatThrownBy(() -> resolveSpaConfiguration(json("/duplicate-url-pattern-spa.config.json")))
                .hasMessageContaining("duplicate");
    }

    @Test
    public void resolveSpaConfiguration_fail_if_missing_leading_slash() {
        assertThatThrownBy(() -> resolveSpaConfiguration(json("/missing-leading-slash-spa.config.json")))
                .hasMessageContaining("invalidForwardTarget");


            // TODO oppdater common validation slik at den kan kaste en
            // felles exception for alle valideringsfeil for en collection av elementer
            //     .hasMessageContaining("invalidUrlPattern");
    }

    @Test
    public void resolveSpaConfiguration_fail_if_incomplete() {
        assertThatThrownBy(() -> resolveSpaConfiguration(json("/incomplete-spa.config.json")))
                .hasMessageContaining("forwardTarget")
                .hasMessageContaining("urlPattern");
    }

    private File json(String name) {
        return new File(getClass().getResource(name).getFile());
    }


}