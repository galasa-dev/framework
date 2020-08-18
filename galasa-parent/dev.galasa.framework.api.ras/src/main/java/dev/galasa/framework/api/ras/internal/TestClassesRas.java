
package dev.galasa.framework.api.ras.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import dev.galasa.framework.spi.ras.RasTestClass;

@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.servlet.pattern=/ras/testclasses" }, name = "TestClasses RAS")

public class TestClassesRas extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Gson gson = new Gson();
    private final List<String> DUMMYQUERY = Arrays.asList("assc","ascc","aasc","ascbundle","descc","ddesc","deesc", "desc" );
    private final List<RasTestClass> TESTCLASSDUMMY = Arrays.asList(new RasTestClass("FirstTest", "Abundle"),new RasTestClass("SecondTest", "BigBundle"),new RasTestClass("ThirdTest", "MiniBundle"),new RasTestClass("ZeroTest", "NanoBundle"));
    // sorting options to search for in querry not testclasses, use equals intead of
    // ==,
    // throw servlet exception, not hide it
    // error not needed, should return empty array with 200

    @Reference
    public IFramework framework; // NOSONAR

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        JsonObject testclasses = new JsonObject();
        JsonArray testObjects = new JsonArray();

        for(String search: DUMMYQUERY){
                if(search.equals("asctest")){
                    TESTCLASSDUMMY.sort(Comparator.comparing(RasTestClass::getTestClass));
                    
                }
                else if(search.equals("desctest")){
                    TESTCLASSDUMMY.sort(Comparator.comparing(RasTestClass::getTestClass).reversed());
                }
                else if(search.equals("ascbundle")){
                    TESTCLASSDUMMY.sort(Comparator.comparing(RasTestClass::getBundleName));
                }
                else if(search.equals("descbundle")){
                    TESTCLASSDUMMY.sort(Comparator.comparing(RasTestClass::getBundleName).reversed());
                }
            }
            for(int i=0;i<TESTCLASSDUMMY.size();i++){
                        JsonObject test = new JsonObject();
                        test.addProperty("testclass",TESTCLASSDUMMY.get(i).getTestClass());
                        test.addProperty("bundle", TESTCLASSDUMMY.get(i).getBundleName());
                        testObjects.add(test);
                    }

        
        

            testclasses.add("testclasses", testObjects);
            

            resp.setStatus(200);
            resp.setContentType("application/json");
            PrintWriter out = resp.getWriter();
            out.print(testclasses);
        
        
        
        
    }
}
