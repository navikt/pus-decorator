package no.nav.pus.decorator.login;

import lombok.Data;
import lombok.experimental.Accessors;

import java.net.URL;

@Data
@Accessors(chain = true)
public class AuthConfig {

    public boolean enforce = true;
    public URL loginUrl;
    public int minSecurityLevel = 4;
    public long minRemainingSeconds = 60 * 20;
}
