package no.nav.pus.decorator.spa;

import no.nav.pus.decorator.ApplicationConfigTest;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class SPAConfigResolverTest {

    @Test
    public void resolveSpaConfiguration_defaul() {
        assertThat(SPAConfigResolver.resolveSpaConfiguration())
                .extracting("forwardTarget", "urlPattern")
                .containsExactly(
                        tuple("/index.html", "/*"),
                        tuple("/demo/index.html", "/demo/*"));


    }

    @Test
    public void resolveSpaConfiguration_nonExistingFile() {
        assertThat(SPAConfigResolver.resolveSpaConfiguration(new File("/non-existing-file.json")))
                .extracting("forwardTarget", "urlPattern")
                .containsExactly(
                        tuple("/index.html", "/*"),
                        tuple("/demo/index.html", "/demo/*"));
    }

    @Test
    public void resolveSpaConfiguration_withFile() {
        assertThat(SPAConfigResolver
                .resolveSpaConfiguration(json("/demo-spa.config.json"))).extracting("forwardTarget", "urlPattern")
                .containsExactly(
                        tuple("app-1.html", "/app1"),
                        tuple("small/smaller-app.html", "/small/app/*"));



    }

    private File json(String name) {
        return new File(ApplicationConfigTest.class.getResource(name).getFile());
    }



}