package dev.galasa.framework.api.runs.internal;

import java.io.IOException;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.servlet.pattern=/allruns" }, name = "Galasa Get Tests")
public class AllRuns extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Log logger = LogFactory.getLog(getClass());

    private final Gson gson = GalasaGsonBuilder.build();

    @Reference
    public IFramework framework; // NOSONAR

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        List<IRun> runs = null;
        try{
            runs = framework.getFrameworkRuns().getAllRuns();
        } catch (FrameworkException e) {
            logger.error("Unable to obtain framework runs", e);
            resp.setStatus(500);
            return;
        }

        resp.setStatus(200);
        resp.setHeader("Content-Type", "Application/json");
        try {
            resp.getWriter().write(gson.toJson(runs));
        } catch (Exception e) {
            logger.fatal("Unable to respond to requester", e);
            resp.setStatus(500);
        }
    }
    
}