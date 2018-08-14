package no.nav.pus.decorator.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static java.util.Optional.empty;

public class NoLoginService implements LoginService {

    @Override
    public Optional<String> getRedirectUrl(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return empty();
    }

}
