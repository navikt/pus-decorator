package no.nav.innholdshenter.common;

import no.nav.innholdshenter.tools.InnholdshenterTools;
import no.nav.sbl.rest.RestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;

public class SimpleEnonicClient implements ContentRetriever {
    private static final Logger logger = LoggerFactory.getLogger(SimpleEnonicClient.class);

    private String baseUrl;

    public SimpleEnonicClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private static String makeFullUrl(String path, String baseUrl) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return baseUrl + path;
    }

    @Override
    public String getPageContent(String path) {
        String url = makeFullUrl(path, baseUrl);
        return getPageContentFullUrl(url);
    }

    private String getPageContentFullUrl(String url) {
        String uniqueRandomUrl = InnholdshenterTools.makeUniqueRandomUrl(url);
        logger.info("Retrieving page content from url {}", url);
        return RestUtils.withClient(c -> c.target(uniqueRandomUrl)
                .request()
                .get(String.class)
        );
    }

    Properties getProperties(String path) {
        String url = makeFullUrl(path, baseUrl);
        return getPropertiesFullUrl(url);
    }

    private Properties getPropertiesFullUrl(String url) {
        String xmlstring = getPageContentFullUrl(url);

        Document document = Jsoup.parse(xmlstring, "", Parser.xmlParser());
        Map<String, String> map = document.select("text").stream().collect(Collectors.toMap(
                element -> unescapeHtml4(element.getElementsByTag("key").first().html()),
                element -> unescapeHtml4(element.getElementsByTag("value").first().html())
        ));

        Properties properties = new Properties();
        properties.putAll(map);
        return properties;
    }

}

