package no.nav.pus.decorator;

import lombok.SneakyThrows;
import no.nav.innholdshenter.filter.DecoratorFilter;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.Charset;

import static no.nav.pus.decorator.FragmentConfig.*;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

public class FragmentCreator {

    private static final String BODY_TEMPLATE = readTemplate("/body-template.html");
    private static final String HEAD_TEMPLATE = readTemplate("/head-template.html");
    private final String frontendLoggerHtml;
    private final String environmentHtml;

    public FragmentCreator(String applicationName) {
        // https://github.com/navikt/fo-frontendlogger
        this.frontendLoggerHtml = "" +
                "<script>\n window.frontendlogger = { " +
                "info: function(){}, warn: function(){}, error: function(){}, event: function(){}};\n" +
                "window.frontendlogger.appname = '" + applicationName + "';\n" +
                "</script>\n" +
                "<script type=\"application/javascript\" src=\"/frontendlogger/logger.js\"></script>";

        this.environmentHtml = "<script>\n" + new EnvironmentScriptGenerator().generate() + "\n</script>";
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

        HeaderType headerType = getOptionalProperty(HEADER_TYPE_PROPERTY).map(HeaderType::valueOf).orElse(HeaderType.WITH_MENU);

        if(headerType == HeaderType.MOBILE_MENU_ONLY){
            String menuStyleHtml = "<style>" +
                    ".topnavsection-wrapper { display: none; }" +
                    "@media (max-width: 750px) {" +
                    ".topnavsection-wrapper { display: inherit; }" +
                    "}" +
                    "</style>";
            head.prepend(menuStyleHtml);
        }

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
        HEADER_FRAGMENT.ifPresent(fragment -> body.getElementById("pagewrapper").prepend(" " + fragment).prepend("{{fragment.skiplinks}}"));
        FOOTER_FRAGMENT.ifPresent(fragment -> body.append(" " + fragment));
        body.prepend("<noscript>Du må aktivere javascript for å kjøre denne appen.</noscript>");
    }

    @SneakyThrows
    static String readTemplate(String uri) {
        return IOUtils.toString(DecoratorFilter.class.getResource(uri), Charset.forName("UTF-8"));
    }

}
