package no.nav.pus.decorator.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Path(AuthenticationResource.AUTHENTICATION_RESOURCE_PATH)
public class AuthenticationResource {

    static final String AUTHENTICATION_RESOURCE_PATH = "/auth";
    static final String LOGIN_PATH = "/login";

    private final LoginService loginService;
    private final String contextPath;

    public AuthenticationResource(LoginService loginService, String contextPath) {
        this.loginService = loginService;
        this.contextPath = contextPath;
    }

    @GET
    public AuthenticationStatusDTO status(
            @Context HttpServletRequest httpServletRequest,
            @Context HttpServletResponse httpServletResponse
    ) {
        return loginService.getStatus(httpServletRequest, httpServletResponse);
    }

    @GET
    @Path(LOGIN_PATH)
    public Response login(
            @Context HttpServletRequest httpServletRequest,
            @Context HttpServletResponse httpServletResponse
    ) {
        URI destinationUrl = loginService.getDestinationUrl(httpServletRequest, httpServletResponse).map(URI::create).orElseGet(() -> goHome(httpServletRequest));
        return Response.temporaryRedirect(destinationUrl).build();
    }

    private URI goHome(HttpServletRequest httpServletRequest) {
        return UriBuilder.fromUri(httpServletRequest.getRequestURL().toString())
                .replacePath(contextPath)
                .build();
    }

}
