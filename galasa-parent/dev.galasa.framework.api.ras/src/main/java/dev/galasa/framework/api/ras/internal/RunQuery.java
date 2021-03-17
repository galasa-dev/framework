package dev.galasa.framework.api.ras.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import org.apache.commons.collections4.ListUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.api.ras.RasRunResult;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasSearchCriteriaBundle;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedFrom;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedTo;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRequestor;
import dev.galasa.framework.spi.ras.RasSearchCriteriaResult;
import dev.galasa.framework.spi.ras.RasSearchCriteriaTestName;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

      Gson gson = GalasaGsonBuilder.build();

      int pageNum = 1;
      int pageSize = 100;

      Map<String, String> paramMap = getParameterMap(req);

      List<IRasSearchCriteria> critList = new ArrayList<>();
      
      String from = "";
      String to = "";
      
	  from = getDefaultStartTime();
	  to = getDefaultEndTime();
	  
      Instant toCrit = null;
      Instant fromCrit = null;
      
      toCrit = Instant.parse(to);
      RasSearchCriteriaQueuedTo toCriteria = new RasSearchCriteriaQueuedTo(toCrit);
      fromCrit = Instant.parse(from);
      RasSearchCriteriaQueuedFrom fromCriteria = new RasSearchCriteriaQueuedFrom(fromCrit);  
      
      
      if (!paramMap.isEmpty()) {
    	  
         if(paramMap.get("page") != null && !paramMap.get("page").equals("")) {
            try {
               pageNum = Integer.parseInt(paramMap.get("page"));
            }catch(Exception e) {

               throw new ServletException("Error parsing integer, ", e);

            }
         }

         if(paramMap.get("size") != null && !paramMap.get("size").equals("")) {
            try{
               pageSize = Integer.parseInt(paramMap.get("size"));
            }catch(Exception e) {

               throw new ServletException("Error parsing integer, ", e);
            }
         }
         
         String requestor = paramMap.get("requestor");
         String testName = paramMap.get("testname");
         String bundle = paramMap.get("bundle");
         String result = paramMap.get("result");
         
         try {
        	 if (paramMap.get("to") != null && !paramMap.get("to").isEmpty()) {
        		 to = paramMap.get("to");
        		 toCrit = Instant.parse(to);
        		 toCriteria = new RasSearchCriteriaQueuedTo(toCrit);
        	 }
        	 if (paramMap.get("from") != null && !paramMap.get("from").isEmpty()) {
            	from = paramMap.get("from");
            	fromCrit = Instant.parse(from);
            	fromCriteria = new RasSearchCriteriaQueuedFrom(fromCrit);
        	 } 
         } catch (Exception e) {

            throw new ServletException("Error parsing Instant, ", e);
         }
         
         if(requestor != null && !requestor.isEmpty()) {
            RasSearchCriteriaRequestor requestorCriteria = new RasSearchCriteriaRequestor(requestor);
            critList.add(requestorCriteria);
         }
         if(testName != null && !testName.isEmpty()) {
            RasSearchCriteriaTestName testNameCriteria = new RasSearchCriteriaTestName(testName);
            critList.add(testNameCriteria);
         }
         if(bundle != null && !bundle.isEmpty()) {
            RasSearchCriteriaBundle bundleCriteria = new RasSearchCriteriaBundle(bundle);
            critList.add(bundleCriteria);
         }
         
         if(result != null && !result.isEmpty()) {
            RasSearchCriteriaResult resultCriteria = new RasSearchCriteriaResult(result);
            critList.add(resultCriteria);
         }
        
      }
      
      critList.add(fromCriteria);
      critList.add(toCriteria);
      
     
      List<RasRunResult> runs = new ArrayList<>();

      try {
         runs = getRuns(critList);
      } catch (Exception e) {

         throw new ServletException("Error retrieving runs, ", e);
      }
      
      
      Collections.sort(runs, Comparator.nullsLast(Comparator.nullsLast(new SortByEndTime())));

      Map<String, String[]> query = req.getParameterMap();

      if(!query.isEmpty()){
         if(!ExtractQuerySort.isAscending(query, "to")) {
            Collections.reverse(runs);
         }
      }


      List<JsonObject> returnArray = new ArrayList<>();

      List<List<RasRunResult>> runList = ListUtils.partition(runs, pageSize);

      int numPages = runList.size();

      int pageIndex = 1;


      if(runList != null) {
         for(List<RasRunResult> list : runList) {

            JsonObject obj = new JsonObject();

            obj.addProperty("pageNum", pageIndex);
            obj.addProperty("pageSize", pageSize);
            obj.addProperty("numPages", numPages);
            obj.addProperty("amountOfRuns", runs.size());

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
  

   private List<RasRunResult> getRuns(List<IRasSearchCriteria> critList) throws ResultArchiveStoreException {

      List<IRunResult> runs = new ArrayList<>();

      IRasSearchCriteria[] criteria = new IRasSearchCriteria[critList.size()];

      critList.toArray(criteria);

      for (IResultArchiveStoreDirectoryService directoryService : framework.getResultArchiveStore().getDirectoryServices()) {

         runs.addAll(directoryService.getRuns(criteria));

      }

      List<RasRunResult> runResults = new ArrayList<>();
      

      for(IRunResult run : runs) {
         runResults.add(RunResultUtility.toRunResult(run, true));
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
   
   private String getDefaultStartTime() {
	   LocalDateTime now = LocalDateTime.now();
	   LocalDateTime start = now.minusHours(24);
	   String from = start.toString().concat("Z");
	   
	   return from;
   }
   
   private String getDefaultEndTime() {
	   LocalDateTime end = LocalDateTime.now();
	   String to = end.toString().concat("Z");
	   
	   return to;
   }
   
   
   class SortByEndTime implements Comparator<RasRunResult> {
      
      @Override
      public int compare(RasRunResult a, RasRunResult b) {
         Instant aEndTime = a.getTestStructure().getEndTime();
         Instant bEndTime = b.getTestStructure().getEndTime();
         
         if(aEndTime == null) {
            if(bEndTime == null) {
               return 0;
            }
            return -1;
         }
         
         if(bEndTime == null) {
            return 1;
         }
         
         return aEndTime.compareTo(bEndTime);
      }
   }


}
