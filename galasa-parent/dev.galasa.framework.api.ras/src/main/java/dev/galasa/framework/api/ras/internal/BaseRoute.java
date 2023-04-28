/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseRoute implements IRoute {
   
   protected Log logger = LogFactory.getLog(this.getClass());

   private final String path;

   public BaseRoute(String path) {
      this.path = path;
   }

   public String getPath() {
      return path;
   }

   protected void sendResponse(HttpServletResponse resp , String json , int status) {
      //Set headers for HTTP Response
      resp.setStatus(status);
      resp.setContentType( "Application/json");
      resp.addHeader("Access-Control-Allow-Origin", "*");
      try {
         PrintWriter out = resp.getWriter();
         out.print(json);
         out.close();
      } catch (Exception e) {
         logger.error("Error trying to set output buffer. Ignoring.",e);
      }
   }
}
