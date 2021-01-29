/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.api.ras.internal;

import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
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

@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/ras/requestors" }, name = "Galasa Requestor microservice")
public class RequestorRas extends HttpServlet {

	private static final long serialVersionUID = 1L;


	@Reference
	public IFramework framework; // NOSONAR

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {


		try {

			//gets string query as hashmap

			Map<String, String[]> query = req.getParameterMap();



			//gets requestors
			List<String> list = getRequestors();

			//sorts list
			Collections.sort(list);

			JsonObject requestors = new JsonObject();   	

			if(!query.isEmpty()) { 
				if(!ExtractQuerySort.isAscending(query, "requestor")) {
					Collections.reverse(list);
				}
			}

			//create json object
			JsonElement json = new Gson().toJsonTree(list);
			requestors.add("requestors", json);

			PrintWriter out = resp.getWriter();
			resp.setContentType( "Application/json");
			resp.setHeader("Access-Control-Allow-Origin", "*");
			out.print(requestors);
			out.close();

		}
		catch(Exception e) {
			throw new ServletException("Error occured during get requestors", e);
		}


	}

	private List<String> getRequestors() throws ResultArchiveStoreException{

		HashSet<String> requestorSet = new HashSet<>();


		for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
			requestorSet.addAll(directoryService.getRequestors());
		}

		//convert to list of strings
		List<String> requestors = new ArrayList<>(requestorSet);

		return requestors;


	}




}