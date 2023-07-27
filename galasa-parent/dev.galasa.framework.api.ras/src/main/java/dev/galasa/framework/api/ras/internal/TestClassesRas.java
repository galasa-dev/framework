/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.api.ras.internal;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

import dev.galasa.framework.api.ras.internal.common.SortQueryParameterChecker;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.ras.internal.verycommon.QueryParameters;
import dev.galasa.framework.api.common.ServletError;
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

	private SortQueryParameterChecker sortQueryParameterChecker = new SortQueryParameterChecker();

	@Reference
	public IFramework framework;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


		QueryParameters queryParams = new QueryParameters(req.getParameterMap());
		List<RasTestClass> classArray = new ArrayList<>();

		try{
			for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {
				classArray.addAll(directoryService.getTests());
			}
		} catch (ResultArchiveStoreException e) {
			throw new ServletException("Problem with retrieving tests", e);
		}

		classArray.sort(Comparator.comparing(RasTestClass::getTestClass));

		/* looking for sort options in query and sorting accordingly */
		try {
			if(!sortQueryParameterChecker.isAscending(queryParams, "testclass")) {
				classArray.sort(Comparator.comparing(RasTestClass::getTestClass).reversed());
			}
		} catch (InternalServletException e) {
			ServletError error = new ServletError(GAL5011_SORT_VALUE_NOT_RECOGNIZED,"testclass");
			throw new ServletException(error);
		}


		/* converting data to json */
		JsonElement json = new Gson().toJsonTree(classArray);
		JsonObject testclasses = new JsonObject();
		testclasses.add("testclasses", json);

		/* setting response status and type */
		resp.setStatus(200);
		resp.setContentType("application/json");
		resp.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = resp.getWriter();
		out.print(testclasses);
		out.close();

	}

}
