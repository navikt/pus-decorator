package no.nav.pus.decorator.spa;

import no.nav.apiapp.util.WarFolderFinderUtil;
import no.nav.pus.decorator.ApplicationConfig;
import no.nav.pus.decorator.config.Config;
import no.nav.pus.decorator.config.ConfigResolver;
import no.nav.sbl.dialogarena.test.junit.SystemPropertiesRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static no.nav.pus.decorator.config.ConfigResolver.resolveConfig;
import static no.nav.pus.decorator.spa.SPAConfigResolver.WEBROOT_PATH_PROPERTY_NAME;
import static org.assertj.core.api.Assertions.*;

public class SPAConfigResolverTest {

    @Rule
    public SystemPropertiesRule systemPropertiesRule = new SystemPropertiesRule();

    public static String getWebappSourceDirectory() {
        return WarFolderFinderUtil.findPath(ApplicationConfig.class).getAbsolutePath();
    }

    @Test
    public void resolveSpaConfiguration_default() {
        systemPropertiesRule.setProperty(WEBROOT_PATH_PROPERTY_NAME, getWebappSourceDirectory());

        assertThat(SPAConfigResolver.resolveSpaConfiguration(resolveConfig()))
                .extracting("forwardTarget", "urlPattern")
                .containsExactly(
                        tuple("/index.html", "/*")
                );
    }

    @Test
    public void parseDecoratorConfiguration_withDemoFile() {
        systemPropertiesRule.setProperty(WEBROOT_PATH_PROPERTY_NAME, new File("src/test/resources/config/demo-webroot").getAbsolutePath());

        assertThat(
                resolveSpaConfiguration("/config/demo-spa.yaml")).extracting("forwardTarget", "urlPattern")
                .containsExactly(
                        tuple("/app-1.html", "/app1"),
                        tuple("/small/smaller-app.html", "/small/app/*"));
    }

    @Test
    public void resolveSpaConfiguration_fail_if_forward_target_does_not_exist() {
        assertThatThrownBy(() -> resolveSpaConfiguration("/config/demo-spa.yaml"))
                .hasMessageContaining("not found");
    }

    @Test
    public void resolveSpaConfiguration_fail_if_duplicated_url_pattern() {
        assertThatThrownBy(() -> resolveSpaConfiguration("/config/duplicate-url-pattern-spa.yaml"))
                .hasMessageContaining("duplicate");
    }

    @Test
    public void resolveSpaConfiguration_fail_if_missing_leading_slash() {
        assertThatThrownBy(() -> resolveSpaConfiguration("/config/missing-leading-slash-spa.yaml"))
                .hasMessageContaining("invalidForwardTarget")
                .hasMessageContaining("invalidUrlPattern");
    }

    @Test
    public void resolveSpaConfiguration_fail_if_incomplete() {
        assertThatThrownBy(() -> resolveSpaConfiguration("/config/incomplete-spa.yaml"))
                .hasMessageContaining("forwardTarget")
                .hasMessageContaining("urlPattern");
    }

    private List<SPAConfig> resolveSpaConfiguration(String name) {
        systemPropertiesRule.setProperty(ConfigResolver.CONFIGURATION_LOCATION_PROPERTY, getClass().getResource(name).getFile());
        Config resolveConfig = resolveConfig();
        return SPAConfigResolver.resolveSpaConfiguration(resolveConfig);
    }

}