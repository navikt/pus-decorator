package no.nav.pus.decorator.config;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.pus.decorator.FooterType;
import no.nav.pus.decorator.HeaderType;

import javax.validation.constraints.NotNull;

import static no.nav.pus.decorator.DecoratorUtils.getNewDecoratorUrl;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

@Data
@Accessors(chain = true)
public class DecoratorConfig {

    public static final String HEADER_TYPE_PROPERTY = "HEADER_TYPE";
    public static final String FOOTER_TYPE_PROPERTY = "FOOTER_TYPE";

    public HeaderType headerType;
    public FooterType footerType;

    public DecoratorConfig () {
        headerType = getHeaderType();
        footerType = getFooterType();
    }

    private static HeaderType getHeaderType() {
        if (getNewDecoratorUrl().isPresent()) {
            return HeaderType.WITH_MENU;
        }
        return getOptionalProperty(HEADER_TYPE_PROPERTY).map(HeaderType::valueOf).orElse(HeaderType.WITH_MENU);
    }

    private static FooterType getFooterType() {
        if (getNewDecoratorUrl().isPresent()) {
            return FooterType.WITH_ALPHABET;
        }
        return getOptionalProperty(FOOTER_TYPE_PROPERTY).map(FooterType::valueOf).orElse(FooterType.WITH_ALPHABET);
    }
}
