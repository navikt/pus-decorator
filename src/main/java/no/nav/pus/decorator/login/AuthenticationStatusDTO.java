package no.nav.pus.decorator.login;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class AuthenticationStatusDTO {
    public long remainingSeconds;
    public Date expirationTime;
}
