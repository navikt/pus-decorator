package no.nav.pus.decorator.spa;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SPAConfig {
    public final String forwardTarget;
    public final String urlPattern;
}
