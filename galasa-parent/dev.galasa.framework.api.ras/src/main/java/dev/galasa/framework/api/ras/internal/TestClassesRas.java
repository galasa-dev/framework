package dev.galasa.framework.api.ras.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.RasTestClass;

@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/ras/testclasses" }, name = "TestClasses RAS")

public class TestClassesRas extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private List<IResultArchiveStoreDirectoryService> archiveStore;

	private IResultArchiveStoreDirectoryService directoryService;

	private static List<RasTestClass> classArray = new ArrayList<>();

	@Reference
	public IFramework framework; // NOSONAR

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map<String, String[]> query = req.getParameterMap();

		/* getting tests */
		archiveStore = framework.getResultArchiveStore().getDirectoryServices();
		
		directoryService=archiveStore.get(archiveStore.size()-1);
		try{
			classArray = directoryService.getTests();
		} catch (ResultArchiveStoreException e) {
			throw new ServletException("Problem with retrieving tests", e);
		}

		/* looking for sort options in query and sorting accordingly */
		if(!query.isEmpty()){


			if(ExtractQuerySort.isAscending(query, "testclass")) {
				classArray.sort(Comparator.comparing(RasTestClass::getTestClass));
			}else if(!ExtractQuerySort.isAscending(query, "testclass")) {
				classArray.sort(Comparator.comparing(RasTestClass::getTestClass).reversed());
			}
		}
		

		/* converting data to json */
		JsonElement json = new Gson().toJsonTree(classArray);
		JsonObject testclasses = new JsonObject();
		testclasses.add("testclasses", json);

		/* setting response status and type */
		resp.setStatus(200);
		resp.setContentType("application/json");
		resp.addHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = resp.getWriter();
		out.print(testclasses);

	}

}
