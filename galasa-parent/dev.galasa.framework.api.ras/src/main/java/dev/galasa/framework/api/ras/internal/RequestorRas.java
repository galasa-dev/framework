/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.api.ras.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

/**
 * CPS API
 * 
 * Allows for CPS properties to be retrieved and added
 * 
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.servlet.pattern=/char" }, name = "Galasa Requestor microservice")
public class RequestorRas extends HttpServlet {
    
    private static final long serialVersionUID = 1L;

 
    @Reference
    public IFramework framework; // NOSONAR

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
       
        try {
            
        PrintWriter out = resp.getWriter();
        resp.setHeader("Content-Type", "Application/json");
        out.print(getRequestors(resp));
        out.flush();
        
        }
        catch(Exception e) {
            
        }
       
       
    }
    
    private JsonObject getRequestors(HttpServletResponse resp) {
        JsonObject requestorsObj = new JsonObject();
        JsonArray requestors = new JsonArray();
        
        requestorsObj.add("requestors", requestors);
        
        return requestorsObj;
    }



}