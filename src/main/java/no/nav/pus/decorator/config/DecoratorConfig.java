package no.nav.pus.decorator.config;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.pus.decorator.FooterType;
import no.nav.pus.decorator.HeaderType;
import no.nav.sbl.util.EnvironmentUtils;

import javax.validation.constraints.NotNull;

import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

@Data
@Accessors(chain = true)
public class DecoratorConfig {

    @NotNull
    public HeaderType headerType = getOptionalProperty("HEADER_TYPE").map(HeaderType::valueOf).orElse(HeaderType.WITH_MENU);

    @NotNull
    public FooterType footerType = getOptionalProperty("FOOTER_TYPE").map(FooterType::valueOf).orElse(FooterType.WITHOUT_ALPHABET);

}
