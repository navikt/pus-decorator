package no.nav.pus.decorator.config;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.net.URL;

@Data
@Accessors(chain = true)
public class RedirectConfig {

    @NotEmpty
    @Pattern(regexp = "/.+")
    public String from;

    @NotNull
    public URL to;

}