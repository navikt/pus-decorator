package no.nav.pus.decorator;

import lombok.SneakyThrows;
import no.nav.innholdshenter.filter.DecoratorFilter;
import no.nav.pus.decorator.config.DecoratorConfig;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.charset.Charset;
import java.util.Optional;

import static no.nav.pus.decorator.ConfigurationService.Feature.FRONTEND_LOGGER;
import static no.nav.pus.decorator.ConfigurationService.isEnabled;

public class FragmentCreator {

    private static final String BODY_TEMPLATE = readTemplate("/body-template.html");
    private static final String HEAD_TEMPLATE = readTemplate("/head-template.html");
    private final String frontendLoggerHtml;
    private final String environmentHtml;
    private final Optional<String> headerFragment;
    private final Optional<String> footerFragment;

    public FragmentCreator(DecoratorConfig decoratorConfig, String applicationName) {
        // https://github.com/navikt/fo-frontendlogger
        this.frontendLoggerHtml = getFrontendLoggerHtml(applicationName);
        this.environmentHtml = "<script>\n" + new EnvironmentScriptGenerator().generate() + "\n</script>";

        this.headerFragment = decoratorConfig.headerType.getFragment();
        this.footerFragment = decoratorConfig.footerType.getFragment();
    }

    private String getFrontendLoggerHtml(String applicationName) {
        if (isEnabled(FRONTEND_LOGGER)) {
            return "" +
                    "<script>\n window.frontendlogger = { " +
                    "info: function(){}, warn: function(){}, error: function(){}, event: function(){}};\n" +
                    "window.frontendlogger.appname = '" + applicationName + "';\n" +
                    "</script>\n" +
                    "<script type=\"application/javascript\" src=\"/frontendlogger/logger.js\"></script>";
        } else {
            return "";
        }
    }

    public String createFragmentTemplate(String orginalHtml) {
        Document document = Jsoup.parse(orginalHtml);
        updateHead(document.head());
        updateBody(document.body());
        return document.html();
    }

    private void updateHead(Element head) {
        head
                .prepend(this.frontendLoggerHtml)
                .prepend(this.environmentHtml)
                .prepend("{{fragment.styles}}{{fragment.scripts}}{{fragment.megamenu-resources}}")
                .prepend(HEAD_TEMPLATE);
    }

    private void updateBody(Element body) {
        Elements children = body.children();
        body.children().clear();
        Document template = Jsoup.parse(BODY_TEMPLATE);
        for (Element child : children) {
            template.getElementById("maincontent").appendChild(child);
        }
        for (Element element : template.body().children()) {
            body.appendChild(element);
        }
        headerFragment.ifPresent(fragment -> body.getElementById("pagewrapper").prepend(" " + fragment).prepend("{{fragment.skiplinks}}"));
        footerFragment.ifPresent(fragment -> body.append(" " + fragment));
        body.prepend("<noscript>Du må aktivere javascript for å kjøre denne appen.</noscript>");
    }

    @SneakyThrows
    static String readTemplate(String uri) {
        return IOUtils.toString(DecoratorFilter.class.getResource(uri), Charset.forName("UTF-8"));
    }

}
