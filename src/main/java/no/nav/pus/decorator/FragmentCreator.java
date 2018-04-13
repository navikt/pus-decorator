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

    public static String createFragmentTemplate(String orginalHtml) {
        Document document = Jsoup.parse(orginalHtml);
        updateHead(document.head());
        updateBody(document.body());
        return document.html();
    }

    private static void updateHead(Element head) {
        HEADER_FRAGMENT.ifPresent(fragment -> head.prepend(" " + fragment));
        head.prepend("{{fragment.styles}}{{fragment.scripts}}{{fragment.megamenu-resources}}");
    }

    private static void updateBody(Element body) {
        Elements children = body.children();
        body.children().clear();
        Document template = Jsoup.parse(TEMPLATE);
        for (Element child : children) {
            template.getElementById("maincontent").appendChild(child);
        }
        for (Element element : template.body().children()) {
            body.appendChild(element);
        }
        FOOTER_FRAGMENT.ifPresent(fragment -> body.append(" " + fragment));
    }

    @SneakyThrows
    static String readTemplate(String uri) {
        return IOUtils.toString(DecoratorFilter.class.getResource(uri), Charset.forName("UTF-8"));
    }

}
