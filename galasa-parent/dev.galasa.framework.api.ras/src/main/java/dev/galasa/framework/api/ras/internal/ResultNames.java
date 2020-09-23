package dev.galasa.framework.api.ras.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.ResultArchiveStoreException;

@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/ras/resultnames" }, name = "Galasa Test Result Names microservice")
public class ResultNames extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private List<IResultArchiveStoreDirectoryService> archiveStore;

	private IResultArchiveStoreDirectoryService directoryService;

	private static @NotNull List<String> resultsList = new ArrayList<>();


	@Reference
	public IFramework framework; // NOSONAR
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map<String, String[]> query = req.getParameterMap();
		archiveStore = framework.getResultArchiveStore().getDirectoryServices();
		directoryService=archiveStore.get(archiveStore.size()-1);
		
		try {
			resultsList = directoryService.getResultNames();
		}
		catch(ResultArchiveStoreException e){
			throw new ServletException("Error occured during get result names", e);
		}
		
		if(!query.isEmpty()) { 
			if(ExtractQuerySort.isAscending(query, "resultnames")) {
				Collections.sort(resultsList);
			}else Collections.sort(resultsList, Collections.reverseOrder());
		}
		
		JsonElement json = new Gson().toJsonTree(resultsList);
		JsonObject resultnames = new JsonObject();
		resultnames.add("resultnames", json);
		
		resp.setStatus(200);
		resp.setContentType("application/json");
		resp.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = resp.getWriter();
		out.print(resultnames);
	}

}
