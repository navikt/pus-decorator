package no.nav.pus.decorator.proxy;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.net.URL;

@Data
@Accessors(chain = true)
public class BackendProxyConfig {

    @NotEmpty
    @Pattern(regexp = "/(\\w|-)+")
    public String contextPath;

    @NotNull
    public URL baseUrl;

    public boolean skipCsrfProtection;

}
