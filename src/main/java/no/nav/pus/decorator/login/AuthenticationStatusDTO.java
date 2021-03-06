package no.nav.pus.decorator.login;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.common.auth.SecurityLevel;

import java.util.Date;

@Data
@Accessors(chain = true)
public class AuthenticationStatusDTO {
    public boolean loggedIn;
    public long remainingSeconds;
    public Date expirationTime;
    public SecurityLevel securityLevel;
}
