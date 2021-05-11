package dev.galasa.framework.api.webui.internal;


import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;


@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property =
{"osgi.http.whiteboard.servlet.pattern=/webui/worklist" }, name = "Galasa Worklist microservice")
public class WorklistQuery extends HttpServlet {

	@Override 
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {

		 Map<String, String> paramMap = getParameterMap(req);
		 
		 //
		 
		 
		 String json = "";
		 
		 //
		 
		 try {
	         PrintWriter out = resp.getWriter();
	         resp.setContentType( "Application/json");
	         resp.addHeader("Access-Control-Allow-Origin", "*");
	         out.print(json);
	         out.close();

	      }catch(Exception e) {

	         throw new ServletException("An error occurred whilst retrieving the Worklist", e);
	      }
		
	}
	
	private Map<String, String> getParameterMap(HttpServletRequest request) {

	      Map<String, String[]> ParameterMap = request.getParameterMap();
	      Map<String, String> newParameterMap = new HashMap<>();
	      for (String parameterName : ParameterMap.keySet()) {
	         String[] values = ParameterMap.get(parameterName);
	         if (values != null && values.length > 0) {
	            newParameterMap.put(parameterName, values[0]);
	         } else {
	            newParameterMap.put(parameterName, null);
	         }
	      }
	   
	      return newParameterMap;
	   }
}
