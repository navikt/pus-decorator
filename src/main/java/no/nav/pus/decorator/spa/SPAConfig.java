package no.nav.pus.decorator.spa;

import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Arrays;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Builder
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@ToString
@Getter
public class SPAConfig {

    @NotEmpty
    @Pattern(regexp = "^/.*")
    private String forwardTarget;

    @NotEmpty
    @Pattern(regexp = "^/.*")
    private String urlPattern;

}
