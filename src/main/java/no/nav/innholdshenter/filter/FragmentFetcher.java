package no.nav.innholdshenter.filter;

import lombok.SneakyThrows;
import no.nav.cache.Cache;
import no.nav.cache.CacheUtils;
import no.nav.innholdshenter.common.ContentRetriever;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;

import static no.nav.cache.CacheConfig.DEFAULT;
import static no.nav.innholdshenter.filter.DecoratorFilterUtils.*;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class FragmentFetcher {

    private final Cache<String, Document> cache;
    private ContentRetriever contentRetriever;
    private String fragmentsUrl;
    private String applicationName;
    private boolean shouldIncludeActiveItem;
    private List<String> fragmentNames;
    private Map<String, String> additionalOptions;
    private String subMenuPath;
    private final HttpServletRequest request;
    private final String originalResponseString;
    private ExtendedConfiguration extendedConfiguration;

    public FragmentFetcher(ContentRetriever contentRetriever,
                           String fragmentsUrl,
                           String applicationName,
                           boolean shouldIncludeActiveItem,
                           String subMenuPath,
                           List<String> fragmentNames,
                           Map<String, String> additionalOptions,
                           HttpServletRequest request,
                           String originalResponseString,
                           ExtendedConfiguration extendedConfiguration,
                           Cache<String, Document> cache) {
        this.contentRetriever = contentRetriever;
        this.fragmentsUrl = fragmentsUrl;
        this.applicationName = applicationName;
        this.shouldIncludeActiveItem = shouldIncludeActiveItem;
        this.fragmentNames = fragmentNames;
        this.additionalOptions = additionalOptions;
        this.subMenuPath = subMenuPath;
        this.request = request;
        this.originalResponseString = originalResponseString;
        this.extendedConfiguration = extendedConfiguration;
        this.cache = cache;
    }

    public Document fetchHtmlFragments(boolean useCache) {
        String url = buildUrl();
        Supplier<Document> runnable = () -> Jsoup.parse(contentRetriever.getPageContent(url));
        return useCache ? cache.get(url, runnable) : runnable.get();
    }

    @SneakyThrows
    public String buildUrl() {
        URIBuilder urlBuilder = new URIBuilder(fragmentsUrl);

        if (applicationName != null) {
            addApplicationName(urlBuilder);
        }

        if (shouldIncludeActiveItem) {
            addActiveItem(urlBuilder);
        }

        String role = extractMetaTag(originalResponseString, "Brukerstatus");
        if (!isEmpty(role)) {
            urlBuilder.addParameter("userrole", role);
        }

        for (String fragmentName : fragmentNames) {
            if (isFragmentSubmenu(fragmentName)) {
                addSubmenuPath(urlBuilder);
            } else {
                urlBuilder.addParameter(fragmentName, "true");
            }
        }

        for (String option : additionalOptions.keySet()) {
            urlBuilder.addParameter(option, additionalOptions.get(option));
        }

        return urlBuilder.build().toString() + getOptionalProperty("EXTRA_DECORATOR_PARAMS").orElse("");
    }

    private void addApplicationName(URIBuilder urlBuilder) {
        String requestUri = request.getRequestURI();
        if (extendedConfiguration != null) {
            Map<String, String> tnsValues = extendedConfiguration.getTnsValues();
            for (String key : tnsValues.keySet()) {
                Matcher matcher = createMatcher(key, requestUri);
                if (matcher.matches()) {
                    urlBuilder.addParameter("appname", tnsValues.get(key));
                    return;
                }
            }
        }

        urlBuilder.addParameter("appname", applicationName);
    }

    private void addActiveItem(URIBuilder urlBuilder) {
        String requestUri = getRequestUriOrAlternativePathBasedOnMetaTag(originalResponseString, request);
        if (extendedConfiguration != null) {
            Map<String, String> menuMap = extendedConfiguration.getMenuMap();
            for (String key : menuMap.keySet()) {
                Matcher matcher = createMatcher(key, requestUri);
                if (matcher.matches()) {
                    urlBuilder.addParameter("activeitem", menuMap.get(key));
                    return;
                }
            }
        }

        urlBuilder.addParameter("activeitem", requestUri);
    }

    private void addSubmenuPath(URIBuilder urlBuilder) {
        String requestUri = getRequestUriOrAlternativePathBasedOnMetaTag(originalResponseString, request);
        if (extendedConfiguration != null) {
            Map<String, String> subMenuPathMap = extendedConfiguration.getSubMenuPathMap();
            for (String key : subMenuPathMap.keySet()) {
                Matcher matcher = createMatcher(key, requestUri);
                if (matcher.matches()) {
                    urlBuilder.addParameter("submenu", subMenuPathMap.get(key));
                    return;
                }
            }
        }

        urlBuilder.addParameter("submenu", subMenuPath);
    }
}
