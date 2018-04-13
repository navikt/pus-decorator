package no.nav.pus.decorator;

import no.nav.innholdshenter.common.SimpleEnonicClient;
import no.nav.innholdshenter.filter.DecoratorFilter;
import no.nav.sbl.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static no.nav.pus.decorator.ApplicationConfig.APPLICATION_NAME;
import static no.nav.pus.decorator.FragmentConfig.FOOTER_FRAGMENT_NAME;
import static no.nav.pus.decorator.FragmentConfig.HEADER_FRAGMENT_NAME;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

public class DecoratorUtils {
    public static final String APPRES_CMS_URL_PROPERTY = "APPRES_CMS_URL";

    private static final String FRAGMENTS_URL = "common-html/v4/navno";
    private static final List<String> NO_DECORATOR_PATTERNS = new ArrayList<>(asList("./rest/.*", ".*/img/.*", ".*/css/.*", ".*/js/.*", ".*/font/.*", ".*selftest.*"));
    public static final String appresUrl = getRequiredProperty(APPRES_CMS_URL_PROPERTY);

    private static final SimpleEnonicClient enonicClient = new SimpleEnonicClient(appresUrl);

    public static DecoratorFilter getDecoratorFilter() {
        DecoratorFilter decoratorFilter = new DecoratorFilter();
        decoratorFilter.setFragmentsUrl(FRAGMENTS_URL);
        decoratorFilter.setContentRetriever(enonicClient);
        decoratorFilter.setApplicationName(APPLICATION_NAME);
        decoratorFilter.setNoDecoratePatterns(NO_DECORATOR_PATTERNS);
        decoratorFilter.setFragmentNames(fragmentNames());
        return decoratorFilter;
    }

    private static List<String> fragmentNames() {
        return of(
                "webstats-ga-notrack",
                HEADER_FRAGMENT_NAME.orElse("header-withmenu"),
                FOOTER_FRAGMENT_NAME.orElse(""),
                "styles",
                "scripts",
                "skiplinks",
                "megamenu-resources"
        ).filter(StringUtils::notNullOrEmpty).collect(toList());
    }

}
