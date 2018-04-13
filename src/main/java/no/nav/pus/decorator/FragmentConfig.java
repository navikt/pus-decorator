package no.nav.pus.decorator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

public class FragmentConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentConfig.class);

    public static final String FOOTER_TYPE_PROPERTY = "FOOTER_TYPE";
    public static final FooterType FOOTER_TYPE = getOptionalProperty(FOOTER_TYPE_PROPERTY).map(FooterType::valueOf).orElse(FooterType.WITHOUT_ALPHABET);
    public static final Optional<String> FOOTER_FRAGMENT = FOOTER_TYPE.getFragment();
    public static final Optional<String> FOOTER_FRAGMENT_NAME = FOOTER_TYPE.getFragmentName();

    public static final String HEADER_TYPE_PROPERTY = "HEADER_TYPE";
    public static final HeaderType HEADER_TYPE = getOptionalProperty(HEADER_TYPE_PROPERTY).map(HeaderType::valueOf).orElse(HeaderType.WITH_MENU);
    public static final Optional<String> HEADER_FRAGMENT = HEADER_TYPE.getFragment();
    public static final Optional<String> HEADER_FRAGMENT_NAME = HEADER_TYPE.getFragmentName();

    static {
        LOGGER.info("footerType={}", FOOTER_TYPE);
        LOGGER.info("headerType={}", HEADER_TYPE);
    }

}
