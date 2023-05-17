/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.routes;

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
}
