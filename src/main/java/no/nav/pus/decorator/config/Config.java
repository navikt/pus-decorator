package no.nav.pus.decorator.config;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.pus.decorator.login.AuthConfig;
import no.nav.pus.decorator.proxy.BackendProxyConfig;
import no.nav.pus.decorator.spa.SPAConfig;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Accessors(chain = true)
public class Config {

    public String contexPath;

    @Valid
    @NotNull
    public DecoratorConfig decorator = new DecoratorConfig();

    @Valid
    public AuthConfig auth;

    @Valid
    public List<SPAConfig> spa;

    @Valid
    public List<BackendProxyConfig> proxy;

    @Valid
    public List<RedirectConfig> redirect;

}
