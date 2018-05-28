package no.nav.pus.decorator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class EnvironmentServlet extends HttpServlet {
    private final EnvironmentScriptGenerator generator = new EnvironmentScriptGenerator();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String envWrapper = generator.generate();

        resp.setContentType("application/javascript");
        resp.getWriter().write(envWrapper);
    }
}
