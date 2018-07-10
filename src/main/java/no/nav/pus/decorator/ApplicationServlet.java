package no.nav.pus.decorator;

import lombok.SneakyThrows;
import no.nav.sbl.rest.RestUtils;
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

public class ApplicationServlet extends HttpServlet {

    private static final Set<String> HEADER_WHITELIST = new HashSet<>(asList(
            LOCATION,
            CONTENT_TYPE
    ));

    private final String contentUrl;
    private final Client client;

    public ApplicationServlet(String contentUrl) {
        this.contentUrl = contentUrl;
        this.client = contentUrl == null ? null : RestUtils.createClient();
    }

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("default");
        String fileRequestPattern = "^(.+\\..{1,4})$";

        if (!request.getRequestURI().matches(fileRequestPattern)) {
            RequestDispatcher index = getServletContext().getRequestDispatcher("/index.html");
            index.forward(request, response);
        } else {
            if (contentUrl != null) {
                getExternalContent(request, response);
            } else {
                dispatcher.forward(request, response);
            }
        }
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