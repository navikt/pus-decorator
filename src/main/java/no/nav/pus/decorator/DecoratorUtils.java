package no.nav.pus.decorator;

import no.nav.innholdshenter.common.SimpleEnonicClient;
import no.nav.innholdshenter.filter.DecoratorFilter;
import no.nav.pus.decorator.config.DecoratorConfig;
import no.nav.sbl.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

public class DecoratorUtils {

    public static final String APPRES_CMS_URL_PROPERTY = "APPRES_CMS_URL";
    private static final String APPRES_FRAGMENT_URL ="common-html/v4/navno";

    public static final String NEW_DECORATOR_URL_PROPERTY = "NAV_DEKORATOREN_URL";
    private static final String NEW_DECORATOR_FRAGMENT_URL = "person/nav-dekoratoren/";

    private static SimpleEnonicClient enonicClient;

    private static final List<String> NO_DECORATOR_PATTERNS = new ArrayList<>(asList("./rest/.*", ".*/img/.*", ".*/css/.*", ".*/js/.*", ".*/font/.*", ".*selftest.*"));

    public static Optional<String> getAppresUrl() {
        return getOptionalProperty(APPRES_CMS_URL_PROPERTY);
    }

    public static Optional<String> getNewDecoratorUrl() {
        return getOptionalProperty(NEW_DECORATOR_URL_PROPERTY);
    }

    private static SimpleEnonicClient getEnonicClient() {
        if (enonicClient == null) {
            enonicClient = new SimpleEnonicClient(getDecoratorUrl());
        }
        return enonicClient;
    }

    public static DecoratorFilter getDecoratorFilter(DecoratorConfig decoratorConfig) {
        DecoratorFilter decoratorFilter = new DecoratorFilter(
                decoratorConfig,
                getFragmentPath(),
                getEnonicClient(),
                fragmentNames(decoratorConfig),
                ApplicationConfig.resolveApplicationName()
        );
        decoratorFilter.setNoDecoratePatterns(NO_DECORATOR_PATTERNS);
        return decoratorFilter;
    }

    public static String getDecoratorUrl() {
        if (getNewDecoratorUrl().isPresent()) {
            return getNewDecoratorUrl().get();
        } else if (getAppresUrl().isPresent()) {
            return getAppresUrl().get();
        }
        throw new IllegalStateException("Fant ingen av propertyene (appres.url, nav.dekoratoren)");
    }

    public static String getFragmentPath () {
        if (getNewDecoratorUrl().isPresent()) {
            return NEW_DECORATOR_FRAGMENT_URL;
        }
        return APPRES_FRAGMENT_URL;
    }

    private static List<String> fragmentNames(DecoratorConfig decoratorConfig) {
        HeaderType headerType = decoratorConfig.headerType;
        FooterType footerType = decoratorConfig.footerType;
        return of(
                "webstats-ga-notrack",
                headerType.getFragmentName().orElse(null),
                footerType.getFragmentName().orElse(null),
                "styles",
                "scripts",
                "skiplinks",
                "megamenu-resources"
        ).filter(StringUtils::notNullOrEmpty).collect(toList());
    }
}
