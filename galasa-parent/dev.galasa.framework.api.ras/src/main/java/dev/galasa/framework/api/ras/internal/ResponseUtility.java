package dev.galasa.framework.api.ras.internal;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import dev.galasa.JsonError;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class ResponseUtility {
   
   private static final Gson gson = GalasaGsonBuilder.build();
   
   public static void sendJsonResponse(String json, int responseCode, HttpServletResponse res) throws IOException {
      
      PrintWriter out = res.getWriter();
      res.setStatus(responseCode);
      res.setContentType("Application/json");
      res.addHeader("Access-Control-Allow-Origin", "*");
      out.print(json);
      out.close();
      
   }
   
   public static void sendTextResponse(String text, int responseCode, HttpServletResponse res) throws IOException {
      PrintWriter out = res.getWriter();
      res.setStatus(responseCode);
      res.setContentType("text/plain");
      res.addHeader("Access-Control-Allow-Origin", "*");
      out.print(text);
      out.close();
   }

   public static void sendError(String errorString, int responseCode, HttpServletResponse res) throws IOException {
      
      PrintWriter out = res.getWriter();
      res.setStatus(responseCode);
      res.setContentType("Application/json");
      res.addHeader("Access-Control-Allow-Origin", "*");
      JsonError error = new JsonError(errorString);
      String jsonError = gson.toJson(error);
      out.print(jsonError);
      out.close();
      
   }
   
}
