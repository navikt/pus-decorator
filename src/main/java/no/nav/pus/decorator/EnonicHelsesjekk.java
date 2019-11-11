package no.nav.pus.decorator;

import no.nav.apiapp.selftest.Helsesjekk;
import no.nav.apiapp.selftest.HelsesjekkMetadata;
import no.nav.innholdshenter.filter.FragmentFetcher;
import no.nav.pus.decorator.config.DecoratorConfig;
import org.eclipse.jetty.server.Request;

import static no.nav.pus.decorator.DecoratorUtils.getDecoratorFilter;

public class EnonicHelsesjekk implements Helsesjekk {

    private final String url;
    private final FragmentFetcher fragmentFetcher;

    public EnonicHelsesjekk(DecoratorConfig decoratorConfig) {
        fragmentFetcher = getDecoratorFilter(decoratorConfig).createFragmentFetcher("", requestMock());
        url = DecoratorUtils.getDecoratorUrl() + "/" + fragmentFetcher.buildUrl();
    }

    @Override
    public void helsesjekk() throws Throwable {
        fragmentFetcher.fetchHtmlFragments(false);
    }

    private Request requestMock() {
        return new Request(null, null);
    }

    @Override
    public HelsesjekkMetadata getMetadata() {
        return new HelsesjekkMetadata(
                "enonic",
                url,
                "henter hode-fot fra enonic",
                true
        );
    }
}
