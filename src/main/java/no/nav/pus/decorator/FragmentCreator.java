package no.nav.pus.decorator;

import lombok.SneakyThrows;
import no.nav.innholdshenter.filter.DecoratorFilter;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.charset.Charset;

import static no.nav.pus.decorator.FragmentConfig.FOOTER_FRAGMENT;
import static no.nav.pus.decorator.FragmentConfig.HEADER_FRAGMENT;

public class FragmentCreator {

    private static final String TEMPLATE = readTemplate("/body-template.html");
    private final String frontendLoggerHtml;

    public FragmentCreator(String applicationName) {
        // https://github.com/navikt/fo-frontendlogger
        this.frontendLoggerHtml = "" +
                "<script>\n" +
                "window.frontendlogger = { info: function(){}, warn: function(){}, error: function(){}};\n" +
                "window.frontendlogger.appname = '" + applicationName + "';\n" +
                "</script>\n" +
                "<script type=\"application/javascript\" src=\"/frontendlogger/logger.js\"></script>";
    }

    public String createFragmentTemplate(String orginalHtml) {
        Document document = Jsoup.parse(orginalHtml);
        updateHead(document.head());
        updateBody(document.body());
        return document.html();
    }

    private void updateHead(Element head) {
        head.prepend("{{fragment.styles}}{{fragment.scripts}}{{fragment.megamenu-resources}}" + frontendLoggerHtml);
    }

    private void updateBody(Element body) {
        Elements children = body.children();
        body.children().clear();
        Document template = Jsoup.parse(TEMPLATE);
        for (Element child : children) {
            template.getElementById("maincontent").appendChild(child);
        }
        for (Element element : template.body().children()) {
            body.appendChild(element);
        }
        HEADER_FRAGMENT.ifPresent(fragment -> body.getElementById("pagewrapper").prepend(" " + fragment).prepend("{{fragment.skiplinks}}"));
        FOOTER_FRAGMENT.ifPresent(fragment -> body.append(" " + fragment));
    }

    @SneakyThrows
    static String readTemplate(String uri) {
        return IOUtils.toString(DecoratorFilter.class.getResource(uri), Charset.forName("UTF-8"));
    }

}
