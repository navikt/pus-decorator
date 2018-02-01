package no.nav.pus.decorator;

import no.nav.apiapp.selftest.Helsesjekk;
import no.nav.apiapp.selftest.HelsesjekkMetadata;
import no.nav.innholdshenter.filter.FragmentFetcher;
import org.eclipse.jetty.server.Request;

import static no.nav.pus.decorator.DecoratorUtils.getDecoratorFilter;

public class EnonicHelsesjekk implements Helsesjekk {

    private final String url;
    private final FragmentFetcher fragmentFetcher;

    public EnonicHelsesjekk() {
        fragmentFetcher = getDecoratorFilter().createFragmentFetcher("", requestMock());
        url = DecoratorUtils.appresUrl + "/" + fragmentFetcher.buildUrl();
    }

    @Override
    public void helsesjekk() throws Throwable {
        fragmentFetcher.fetchHtmlFragments();
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
