package no.nav.innholdshenter.common;

import no.nav.innholdshenter.tools.InnholdshenterTools;
import org.apache.commons.validator.routines.UrlValidator;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class RandomizeURLTest {
    public static final String RANDOMSTRING = "AAAb312RAWEFF";

    private static final String SERVER = "http://www-t1.nav.no:9000/";
    private static final String PATH = "systemsider/ApplicationFrame";
    private static final String URLPARAMS = "?1&fdsafad=321&fine=cool";

    @Test
    public void testGenerateRandomURL() {
        String url = SERVER + PATH;
        String randomUrl = InnholdshenterTools.makeUniqueRandomUrl(url, RANDOMSTRING);

        UrlValidator validator = new UrlValidator();
        assertTrue(validator.isValid(randomUrl));
        assertEquals(url + "?sid=" + RANDOMSTRING, randomUrl);
    }

    @Test
    public void testGenerateRandomURLWithLeadingParams() {
        String url = SERVER + PATH + URLPARAMS;
        String randomUrl = InnholdshenterTools.makeUniqueRandomUrl(url, RANDOMSTRING);

        assertTrue(randomUrl.contains("sid=" + RANDOMSTRING));
        UrlValidator validator = new UrlValidator();
        assertTrue(validator.isValid(randomUrl));
    }
}
