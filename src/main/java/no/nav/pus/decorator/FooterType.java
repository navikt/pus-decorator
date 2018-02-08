package no.nav.pus.decorator;

import java.util.Optional;

import static no.nav.sbl.util.StringUtils.of;

@SuppressWarnings("unused")
public enum FooterType {
    NO_FOOTER(null),
    WITHOUT_ALPHABET("footer"),
    WITH_ALPHABET("footer-withmenu");

    private final String fragmentName;

    private FooterType(String fragmentName) {
        this.fragmentName = fragmentName;
    }

    public Optional<String> getFragmentName() {
        return of(fragmentName);
    }

    public Optional<String> getFragment() {
        return getFragmentName().map(f -> String.format("{{fragment.%s}}", f));
    }

}

