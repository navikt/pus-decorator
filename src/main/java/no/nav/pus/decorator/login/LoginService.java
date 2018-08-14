package no.nav.pus.decorator.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public interface LoginService {

    Optional<String> getRedirectUrl(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);

}
