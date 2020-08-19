
package dev.galasa.framework.api.ras.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.ras.RasTestClass;

@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.servlet.pattern=/ras/testclasses" }, name = "TestClasses RAS")

public class TestClassesRas extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /* a dummy list of testclasses and bundles */
    private final List<RasTestClass> TESTCLASSDUMMY = Arrays.asList(new RasTestClass("FirstTest", "Abundle"),new RasTestClass("SecondTest", "BigBundle"),new RasTestClass("ThirdTest", "MiniBundle"),new RasTestClass("ZeroTest", "NanoBundle"));
    

    @Reference
    public IFramework framework; // NOSONAR

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String[]> query = req.getParameterMap();

        /* looking for sort options in query and sorting accordingly */
        if(ExtractQuerySort.isAscending(query, "testclass")) {
    		TESTCLASSDUMMY.sort(Comparator.comparing(RasTestClass::getTestClass));
    	}else if(!ExtractQuerySort.isAscending(query, "testclass")) {
    		TESTCLASSDUMMY.sort(Comparator.comparing(RasTestClass::getTestClass).reversed());
        }
        /* converting data to json */
            JsonElement json = new Gson().toJsonTree(TESTCLASSDUMMY);
            JsonObject testclasses = new JsonObject();
            testclasses.add("testclasses", json);
            
        /* setting response status and type */
            resp.setStatus(200);
            resp.setContentType("application/json");
            PrintWriter out = resp.getWriter();
            out.print(testclasses);
        
    }
}
