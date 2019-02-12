package no.nav.pus.decorator.spa;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@Builder
@Accessors(chain = true)
public class SPAConfig {

    @NotEmpty
    @Pattern(regexp = "^/.*")
    private String forwardTarget;

    @NotEmpty
    @Pattern(regexp = "^/.*")
    private String urlPattern;

}
