package no.nav.pus.decorator.login;

import no.nav.common.auth.Subject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static java.util.Optional.empty;

public class NoLoginService implements LoginService {

    @Override
    public Optional<String> getLoginRedirectUrl(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return empty();
    }

    @Override
    public Optional<String> getDestinationUrl(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return empty();
    }

    @Override
    public AuthenticationStatusDTO getStatus(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return null;
    }

    @Override
    public Optional<Subject> authenticate(HttpServletRequest request, HttpServletResponse response) {
        return empty();
    }

}
