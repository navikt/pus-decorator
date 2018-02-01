package no.nav.pus.decorator;

import no.nav.innholdshenter.common.SimpleEnonicClient;
import no.nav.innholdshenter.filter.DecoratorFilter;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.pus.decorator.ApplicationConfig.APPLICATION_NAME;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

public class DecoratorUtils {
    public static final String APPRES_CMS_URL_PROPERTY = "APPRES_CMS_URL";

    private static final String FRAGMENTS_URL = "common-html/v4/navno";
    private static final List<String> NO_DECORATOR_PATTERNS = new ArrayList<>(asList("./rest/.*", ".*/img/.*", ".*/css/.*", ".*/js/.*", ".*/font/.*", ".*selftest.*"));
    private static final List<String> FRAGMENT_NAMES = new ArrayList<>(asList("webstats-ga-notrack", "header-withmenu", "footer", "styles", "scripts", "skiplinks", "megamenu-resources"));
    public static final String appresUrl = getRequiredProperty(APPRES_CMS_URL_PROPERTY);

    private static final SimpleEnonicClient enonicClient = new SimpleEnonicClient(appresUrl);

    public static DecoratorFilter getDecoratorFilter() {
        DecoratorFilter decoratorFilter = new DecoratorFilter();
        decoratorFilter.setFragmentsUrl(FRAGMENTS_URL);
        decoratorFilter.setContentRetriever(enonicClient);
        decoratorFilter.setApplicationName(APPLICATION_NAME);
        decoratorFilter.setNoDecoratePatterns(NO_DECORATOR_PATTERNS);
        decoratorFilter.setFragmentNames(FRAGMENT_NAMES);
        return decoratorFilter;
    }
}
