package no.nav.pus.decorator;

import no.nav.innholdshenter.common.SimpleEnonicClient;
import no.nav.innholdshenter.filter.DecoratorFilter;
import no.nav.pus.decorator.config.DecoratorConfig;
import no.nav.sbl.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

public class DecoratorUtils {
    public static final String APPRES_CMS_URL_PROPERTY = "APPRES_CMS_URL";

    private static final String FRAGMENTS_URL = "common-html/v4/navno";
    private static final List<String> NO_DECORATOR_PATTERNS = new ArrayList<>(asList("./rest/.*", ".*/img/.*", ".*/css/.*", ".*/js/.*", ".*/font/.*", ".*selftest.*"));
    public static final String appresUrl = getRequiredProperty(APPRES_CMS_URL_PROPERTY);

    private static final SimpleEnonicClient enonicClient = new SimpleEnonicClient(appresUrl);

    public static DecoratorFilter getDecoratorFilter(DecoratorConfig decoratorConfig) {
        DecoratorFilter decoratorFilter = new DecoratorFilter(
                decoratorConfig,
                FRAGMENTS_URL,
                enonicClient,
                fragmentNames(decoratorConfig),
                ApplicationConfig.resolveApplicationName()
        );
        decoratorFilter.setNoDecoratePatterns(NO_DECORATOR_PATTERNS);
        return decoratorFilter;
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
