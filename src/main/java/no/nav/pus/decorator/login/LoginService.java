package no.nav.pus.decorator.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public interface LoginService {

    AuthenticationStatusDTO getStatus(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
    Optional<String> getLoginRedirectUrl(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
    Optional<String> getDestinationUrl(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);

}
