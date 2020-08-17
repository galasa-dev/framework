
package dev.galasa.framework.api.ras.internal;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.spi.IFramework;

@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
    "osgi.http.whiteboard.servlet.pattern=/ras/testclasses" }, name = "TestClasses RAS")

public class TestClassesRas extends HttpServlet{

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    private Gson gson = new Gson();
    private final List<String> DUMMYQUERY = Arrays.asList("requestor", "from", "to", "testclasses", "page", "size");
    private final List<String> TESTCLASSDUMMY = Arrays.asList("testclass","xxxxxxx","bundle","yyyyyy","testclass","xxxxxxx","bundle","yyyyyy");

    @Reference
    public IFramework framework; // NOSONAR
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        boolean found = false;
        JsonObject testclasses = new JsonObject();
        JsonArray testObjects = new JsonArray();
        try{ 
            for(String search: DUMMYQUERY){
                if(search=="testclasses"){
                    for(int i=0;i<TESTCLASSDUMMY.size();i++){
                        JsonObject test = new JsonObject();
                        test.addProperty(TESTCLASSDUMMY.get(i),TESTCLASSDUMMY.get(i+1));
                        testObjects.add(test);
                        i++;
                    }
                    
                    testclasses.add(search, testObjects);
                    found = true;
                }
            }
            if(!found){
                resp.setStatus(500);
            }else{

            resp.setStatus(200);
            resp.setContentType("application/json");
            PrintWriter out = resp.getWriter();
            out.print(testclasses);
        }
        }
        catch (Exception e){

        }
        
        
    }
}
