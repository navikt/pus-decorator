package no.nav.pus.decorator.security;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InternalProtectionFilterTest {

    @Test
    public void isAllowedAccessToInternal() {
        assertNoAccess("myapp.nav.no");
        assertNoAccess("tjenester.nav.no");
        assertNoAccess("evil.com");
        assertNoAccess("174.148.185.236");
        assertNoAccess("10.8.81.131");
        assertNoAccess("10.5.81.131");

        assertAccess("192.168.1.1");
        assertAccess("192.168.255.255");
        assertAccess("myapp.nais.oera.no");
        assertAccess("myapp.nais.oera-q.local");
        assertAccess("myapp.nais.adeo.no");
        assertAccess("myapp.nais.preprod.local");
        assertAccess("myapp-q0.nais.oera-q.local");
        assertAccess("localhost");
        assertAccess("10.6.81.131");
        assertAccess("10.7.5.123");
    }

    @Test
    public void isPublicPath() {
        assertThat(InternalProtectionFilter.isPublicPath("/internal/isAlive")).isTrue();
        assertThat(InternalProtectionFilter.isPublicPath("/internal/selftest")).isFalse();
    }


    private void assertNoAccess(String hostName) {
        assertThat(InternalProtectionFilter.isAllowedAccessToInternal(hostName)).isFalse();
    }

    private void assertAccess(String hostName) {
        assertThat(InternalProtectionFilter.isAllowedAccessToInternal(hostName)).isTrue();
    }

}