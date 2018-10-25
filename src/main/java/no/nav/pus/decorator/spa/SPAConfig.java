package no.nav.pus.decorator.spa;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Value
@Builder
public class SPAConfig {

    @NotEmpty
    @Pattern(regexp = "^/.*")
    public final String forwardTarget;

    @NotEmpty
    @Pattern(regexp = "^/.*")
    public final String urlPattern;
}
