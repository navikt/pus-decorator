package no.nav.pus.decorator;

import lombok.SneakyThrows;
import no.nav.pus.decorator.login.LoginService;
import no.nav.sbl.featuretoggle.unleash.UnleashService;
import no.nav.sbl.rest.RestUtils;
import no.nav.sbl.util.EnvironmentUtils;
import org.apache.commons.io.IOUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

public class ApplicationServlet extends HttpServlet {

    private static final Set<String> HEADER_WHITELIST = new HashSet<>(asList(
            LOCATION,
            CONTENT_TYPE
    ));

    private static final String CSP_DIRECTIVES = CspService.generateCspDirectives();
    private static final boolean DISABLE_CSP = getOptionalProperty("DISABLE_CSP").map(Boolean::parseBoolean).orElse(false);

    private final String contentUrl;
    private final Client client;
    private final LoginService loginService;
    private final String forwardTarget;
    private final UnleashService unleashService;
    private final String cspFeatureToggleName = EnvironmentUtils.requireApplicationName() + ".csp";

    public ApplicationServlet(LoginService loginService, String contentUrl, String forwardTarget, UnleashService unleashService) {
        this.loginService = loginService;
        this.contentUrl = contentUrl;
        this.client = contentUrl == null ? null : RestUtils.createClient();
        this.forwardTarget = forwardTarget;
        this.unleashService = unleashService;
    }

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("default");
        String fileRequestPattern = "^(.+\\..{1,4})$";

        if (!request.getRequestURI().matches(fileRequestPattern)) {
            String redirectUrl = loginService.getLoginRedirectUrl(request, response).orElse(null);
            if (redirectUrl != null) {
                response.sendRedirect(redirectUrl);
            } else {
                RequestDispatcher index = getServletContext().getRequestDispatcher(forwardTarget);

                if (!DISABLE_CSP) {
                    if (isEnabled(cspFeatureToggleName)) {
                        response.addHeader("Content-Security-Policy", CSP_DIRECTIVES);
                    } else if (isEnabled("pus-decorator.csp-reporting")) {
                        response.addHeader("Content-Security-Policy-Report-Only", CSP_DIRECTIVES);
                    }
                }
                response.addHeader("X-Content-Type-Options", "nosniff");
                response.addHeader("X-Frame-options", "SAMEORIGIN");
                response.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                response.addHeader("X-XSS-Protection", "1; mode=block");
                index.forward(request, response);
            }
        } else {
            if (contentUrl != null) {
                getExternalContent(request, response);
            } else {
                dispatcher.forward(request, response);
            }
        }
    }

    private boolean isEnabled(String toggleName) {
        return unleashService != null && unleashService.isEnabled(toggleName);
    }

    @SneakyThrows
    private void getExternalContent(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Response targetResponse = client.target(contentUrl)
                .path(httpServletRequest.getRequestURI())
                .request()
                .get();
        httpServletResponse.setStatus(targetResponse.getStatus());
        targetResponse.getStringHeaders().forEach((header, values) -> {
            if (HEADER_WHITELIST.contains(header)) {
                values.forEach(value -> httpServletResponse.setHeader(header, value));
            }
        });
        IOUtils.copy(targetResponse.readEntity(InputStream.class), httpServletResponse.getOutputStream());
    }

}