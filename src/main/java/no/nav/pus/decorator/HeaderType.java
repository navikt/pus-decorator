package no.nav.pus.decorator;

import java.util.Optional;

import static no.nav.sbl.util.StringUtils.of;

@SuppressWarnings("unused")
public enum HeaderType {
    NO_HEADER(null),
    WITHOUT_MENU("header"),
    WITH_MENU("header-withmenu");

    private final String fragmentName;

    private HeaderType(String fragmentName) {
        this.fragmentName = fragmentName;
    }

    public Optional<String> getFragmentName() {
        return of(fragmentName);
    }

    public Optional<String> getFragment() {
        return getFragmentName().map(f -> String.format("{{fragment.%s}}", f));
    }

}

