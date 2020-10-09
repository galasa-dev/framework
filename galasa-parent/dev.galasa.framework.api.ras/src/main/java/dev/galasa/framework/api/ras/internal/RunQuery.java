package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import org.apache.commons.collections4.ListUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.JsonError;
import dev.galasa.api.run.RunResult;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedFrom;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedTo;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRequestor;
import dev.galasa.framework.spi.ras.RasSearchCriteriaTestName;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/ras/run" }, name = "Galasa Runs microservice")
public class RunQuery extends HttpServlet {

   @Reference
   IFramework framework;

   private static final long serialVersionUID = 1L;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

      Gson gson = new Gson();

      int pageNum = -1;
      int pageSize = 100;

      Map<String, String> paramMap = getParameterMap(req);

      List<IRasSearchCriteria> critList = new ArrayList<>();

      if(!paramMap.isEmpty()) {

         if(paramMap.get("page") != null && !paramMap.get("page").equals("")) {
            try {
               pageNum = Integer.parseInt(paramMap.get("page"));
            }catch(Exception e) {

               throw new ServletException("Error parsing integer, ", e);

            }
         }

         if(paramMap.get("pageSize") != null && !paramMap.get("pageSize").equals("")) {
            try{
               pageSize = Integer.parseInt(paramMap.get("pageSize"));
            }catch(Exception e) {

               throw new ServletException("Error parsing integer, ", e);
            }
         }

         String requestor = paramMap.get("requestor");
         String to = paramMap.get("to");
         String from = paramMap.get("from");
         String testName = paramMap.get("testName");

         Instant toCrit = null;
         Instant fromCrit = null;
         try {
            if(to != null) {
               toCrit = Instant.parse(to);
               RasSearchCriteriaQueuedTo toCriteria = new RasSearchCriteriaQueuedTo(toCrit);
               critList.add(toCriteria);
            }
            if(from != null) {
               fromCrit = Instant.parse(from);
               RasSearchCriteriaQueuedFrom fromCriteria = new RasSearchCriteriaQueuedFrom(fromCrit);
               critList.add(fromCriteria);
            }
         }catch(Exception e) {

            throw new ServletException("Error parsing Instant, ", e);
         }
         if(requestor != null) {
            RasSearchCriteriaRequestor requestorCriteria = new RasSearchCriteriaRequestor(requestor);
            critList.add(requestorCriteria);
         }
         if(testName != null) {
            RasSearchCriteriaTestName testNameCriteria = new RasSearchCriteriaTestName(testName);
            critList.add(testNameCriteria);
         }


      }

      List<RunResult> runs = new ArrayList<>();

      try {
         runs = getRuns(critList);
      } catch (Exception e) {

         throw new ServletException("Error retrieving runs, ", e);
      }


      runs.sort(Comparator.nullsLast(Comparator.comparing(RunResult::getEnd, Comparator.nullsLast(Comparator.naturalOrder()))));

      Map<String, String[]> query = req.getParameterMap();

      if(!query.isEmpty()){
         if(!ExtractQuerySort.isAscending(query, "to")) {
            runs.sort(Comparator.nullsLast(Comparator.comparing(RunResult::getEnd, Comparator.nullsLast(Comparator.naturalOrder()))).reversed());
         }
      }


      List<JsonObject> returnArray = new ArrayList<>();

      List<List<RunResult>> runList = ListUtils.partition(runs, pageSize);

      int numPages = runList.size();

      int pageIndex = 1;

      if(runList != null) {
         for(List<RunResult> list : runList) {

            JsonObject obj = new JsonObject();

            obj.addProperty("pageNum", pageIndex);
            obj.addProperty("pageSize", pageSize);
            obj.addProperty("numPages", numPages);

            JsonElement tree = gson.toJsonTree(list);

            obj.add("runs", tree);

            returnArray.add(obj);

            pageIndex+=1;
         }
      }

      String json = "";

      if(returnArray.size() != 0) {
         try {
            json = gson.toJson(returnArray.get(pageNum-1));
         }catch(Exception e) {

            throw new ServletException("Error retrieving page, ", e);

         }	
      }

      try {
         PrintWriter out = resp.getWriter();
         resp.setContentType( "Application/json");
         resp.addHeader("Access-Control-Allow-Origin", "*");
         out.print(json);
         out.close();

      }catch(Exception e) {

         throw new ServletException("An error occurred whilst retrieving runs", e);
      }

   }

   private List<RunResult> getRuns(List<IRasSearchCriteria> critList) throws ResultArchiveStoreException {

      List<IRunResult> runs = new ArrayList<>();

      IRasSearchCriteria[] criteria = new IRasSearchCriteria[critList.size()];

      critList.toArray(criteria);

      for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {

         runs.addAll(directoryService.getRuns(criteria));

      }

      List<RunResult> runResults = new ArrayList<>();

      for(IRunResult run : runs) {
         runResults.add(RunResultUtility.toRunResult(run));
      }

      return runResults;
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
