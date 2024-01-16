/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.*;

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

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.resources.ResourceNameValidator;
import dev.galasa.framework.spi.*;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

/**
 * CPS API
 * 
 * Allows for CPS properties to be retrieved and added
 * 
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.servlet.pattern=/cps/namespace/*" }, name = "Galasa CPS")
public class AccessCps extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Normally this logger would be private, but we made it protected so that
    // junit tests can set it to capture logging, without the need to use
    // byte-code manipulation used by Mockito... which messes with the class
    // loading in some circumstances.
    protected Log logger = LogFactory.getLog(getClass());

    private final Gson gson = GalasaGsonBuilder.build();

    // We match on things which are not slash (^/) so we can do finder-grained
    // validation of object names later in the code.
    // That way we can centralise the validation to share it between various
    // parts of the ecosystem which do a similar job.
    private static final Pattern pattern1 = Pattern.compile("\\/?");
    private static final Pattern pattern2 = Pattern.compile("\\/([^/]*)/?");
    private static final Pattern pattern3 = Pattern.compile("\\/([^/]*)/prefix/([^/]*)/suffix/([^/]*)/?");
    private static final Pattern pattern4 = Pattern.compile("\\/([^/]*)/property/([^/]*)/?");

    @Reference
    public IFramework framework; // NOSONAR

    private static final ResourceNameValidator nameValidator = new ResourceNameValidator();

    /**
     * Some namespaces are hidden from view, and should not be exposed to a user.
     *
     * If the user queries them, they get resource-not-found, as if they aren't there.
     */
    private final static Set<String> hiddenNameSpaces = new HashSet<>();
    static {
        hiddenNameSpaces.add("dss");
    }

    /**
     * Some namespaces are able to be set, but cannot be queried.
     *
     * When they are queried, the values are redacted
     */
    private final static Set<String> writeOnlyNameSpaces = new HashSet<>();
    static {
        writeOnlyNameSpaces.add("secure");
    }

    /**
     * The value returned if anyone queries any property values in a write-only namespace.
     */
    private static final String REDACTED_PROPERTY_VALUE = "********";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            resp.setHeader("Content-Type", "application/json");

            Matcher matcher1 = pattern1.matcher(req.getPathInfo());
            if (matcher1.matches()) {
                getNamespaces(resp);
                return;
            }
            Matcher matcher2 = pattern2.matcher(req.getPathInfo());
            if (matcher2.matches()) {
                String namespace = matcher2.group(1);
                nameValidator.assertNamespaceCharPatternIsValid(namespace);
                getNamespaceProperties(resp,namespace);
                return;
            }
            Matcher matcher3 = pattern3.matcher(req.getPathInfo());
            if (matcher3.matches()) {
                String namespace = matcher3.group(1);
                nameValidator.assertNamespaceCharPatternIsValid(namespace);
                String prefix = matcher3.group(2);
                nameValidator.assertPropertyCharPatternPrefixIsValid(prefix);
                String suffix = matcher3.group(3);
                nameValidator.assertPropertyCharPatternSuffixIsValid(suffix);
                getCPSProperty(resp, namespace, prefix, suffix, req.getQueryString());
                return;
            }

            sendError(resp, "Invalid GET URL - " + req.getPathInfo()
                     , HttpServletResponse.SC_BAD_REQUEST // Bad Request
                     );

        } catch (IOException e) {
            sendServerInternalError(resp, e);
        } catch (InternalServletException e) {
            String message = e.getMessage();
            logger.info(message,e);
            sendError(resp, message
                , e.getHttpFailureCode()
                );
        } catch(FrameworkException e ) {
            String message = e.getMessage();
            logger.info(message,e);
            sendError(resp, message
                , HttpServletResponse.SC_BAD_REQUEST
                );
        }
    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            resp.setHeader("Content-Type", "application/json");
            Matcher matcher4 = pattern4.matcher(req.getPathInfo());

            if (matcher4.matches()) {
                String namespace = matcher4.group(1);
                nameValidator.assertNamespaceCharPatternIsValid(namespace);

                String propertyName = matcher4.group(2);
                nameValidator.assertPropertyNameCharPatternIsValid(propertyName);

                addCPSProperty(resp, req, namespace, propertyName);
                return;
            }
            sendError(resp, "Invalid PUT URL - " + req.getPathInfo()
                     ,400 // Bad Request
                     );

        } catch (IOException e) {
            sendServerInternalError(resp, e);
        } catch (FrameworkException e) {
            switch( e.getErrorCode() ) {

                case INVALID_PROPERTY:
                case INVALID_NAMESPACE:
                    sendError(resp, MessageFormat.format("Invalid request: {0}",e.getMessage())
                            , 400 // Bad request.
                    );
                    break;

                case UNKNOWN:
                default:
                    sendServerInternalError(resp, e);
            }
        }
    }

    private void getNamespaces(HttpServletResponse resp) throws IOException, ConfigurationPropertyStoreException {
        logger.debug("Getting the list of namespaces");
        JsonArray namespaceArray = new JsonArray();
        List<String> namespaces = framework.getConfigurationPropertyService("framework").getCPSNamespaces();
        for (String name : namespaces) {
            if ( ! hiddenNameSpaces.contains(name) ) {
                namespaceArray.add(name);
            }
        }
        resp.getWriter().write(gson.toJson(namespaceArray));
        resp.setStatus(200);
    }

    private void getNamespaceProperties(HttpServletResponse resp, String namespace)
            throws IOException, ConfigurationPropertyStoreException {

        if (hiddenNameSpaces.contains(namespace)) {

            logger.info(MessageFormat.format("User tried to get properties from protected namespace \"{0}\""
                    ,namespace));

            String messageTemplate = "Namespace ''{0}'' is not found.";
            String message = MessageFormat.format(messageTemplate, namespace);
            sendError(resp, message , 404 ); // Resource not found
            return;
        }

        JsonArray propertyArray = new JsonArray();
        Map<String, String> properties = framework.getConfigurationPropertyService(namespace).getAllProperties();
        for (String prop : properties.keySet()) {
            JsonObject cpsProp = new JsonObject();
            cpsProp.addProperty("name", prop);
            cpsProp.addProperty("value", getProtectedValue(properties.get(prop),namespace));
            propertyArray.add(cpsProp);
        }
        resp.getWriter().write(gson.toJson(propertyArray));
        resp.setStatus(200);
    }

    /**
     * When we are about to return a value to the user, just check to see if we should be
     * protected this value by redacting it, so the user can't read the actual value.
     *
     * @param actualValue The value we return if we don't redact the value for this namespace.
     * @param namespace The namespace. Some namespaces are write-only.
     * @return The redacted value if the namespace is write-only, otherwise the actualValue which is passed-in.
     */
    private String getProtectedValue(String actualValue , String namespace) {
        String protectedValue ;
        if (writeOnlyNameSpaces.contains(namespace)) {
            // The namespace is protected, write-only, so should not be readable.
            protectedValue = REDACTED_PROPERTY_VALUE;
        } else {
            protectedValue = actualValue ;
        }
        return protectedValue ;
    }


    private void getCPSProperty(HttpServletResponse resp, String namespace, String prefix, String suffix,
            String infixQuery) throws IOException, ConfigurationPropertyStoreException {

        if (hiddenNameSpaces.contains(namespace)) {

            logger.info(MessageFormat.format("User tried to get properties from protected namespace \"{0}\""
                    ,namespace));

            String messageTemplate = "Namespace ''{0}'' is not found.";
            String message = MessageFormat.format(messageTemplate, namespace);
            sendError(resp, message , 404 ); // Resource not found
            return;
        }

        String[] infixArray = null;
        if (infixQuery == null) {
            infixArray = new String[0];
        } else {
            String[] queries = infixQuery.split("&");
            List<String> infixes = new ArrayList<>();
            for (String pair : queries) {
                String[] keyValue = pair.split("=");
                if (!keyValue[0].equals("infix")) {
                    logger.error("Invalid Infix in URL");
                    resp.setStatus(400);
                    return;
                }
                infixes.add(keyValue[1]);
            }
            infixArray = infixes.toArray(new String[0]);
        }
        JsonObject respJson = new JsonObject();

        IConfigurationPropertyStoreService cpsService = framework.getConfigurationPropertyService(namespace);

        // Get the value of the property from a search of the property namespace...
        String propValue = cpsService.getProperty(prefix, suffix, infixArray);

        // Try to work out which key was used to find that value.
        // Note: This is buggy. We have no guarantee that the value we find above won't exist
        // as a value for multiple property keys. So the following logic may well pick the wrong key
        // to go with the right value.
        // Defect raised to sort this out somehow... https://github.com/galasa-dev/projectmanagement/issues/1289
        Map<String, String> pairs = cpsService.getAllProperties();
        for (String key : pairs.keySet()) {
            if (pairs.get(key).equals(propValue) && key.startsWith(namespace + "." + prefix) && key.endsWith(suffix)) {
                respJson.addProperty("name", key);
                respJson.addProperty("value", getProtectedValue(pairs.get(key),namespace));
                break;
            }
        }
        resp.getWriter().write(gson.toJson(respJson));
        resp.setStatus(200);
    }

    private void addCPSProperty(HttpServletResponse resp, HttpServletRequest req, String namespace, String property)
            throws IOException, ConfigurationPropertyStoreException {

        if (hiddenNameSpaces.contains(namespace)) {
            logger.info(MessageFormat.format("User tried to set properties in a protected namespace \"{0}\""
                    ,namespace));

            String messageTemplate = "Namespace ''{0}'' is not found.";
            String message = MessageFormat.format(messageTemplate, namespace);
            sendError(resp, message , 404 ); // Resource not found
            return;
        }

        JsonObject reqJson = gson.fromJson(new InputStreamReader(req.getInputStream()),JsonObject.class);
        if(!property.equals(reqJson.get("name").getAsString())) {
            sendError(resp, "Different CPS property name in url and request: " + property + ", " + reqJson.get("name"));
        } else {
            IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService(namespace);
            cps.setProperty(reqJson.get("name").getAsString(), reqJson.get("value").getAsString());
            resp.setStatus(200);
            PrintWriter writer = resp.getWriter();
            writer.write(gson.toJson(reqJson));
            writer.flush();
        }
    }

    public void sendServerInternalError(HttpServletResponse resp, Exception e) {
        logger.error(e.getMessage() ,e);
        // We should never return the stack trace on a REST call.
        sendError(resp,MessageFormat.format("Internal server error. {0}",e.getMessage()));
        resp.setStatus(500); // Internal server error.
    }

    public void sendError(HttpServletResponse resp, String errorMessage ) {
        sendError(resp, errorMessage, 500);
    }

    public void sendError(HttpServletResponse resp, String errorMessage, int httpStatusCode ) {
        resp.setStatus(httpStatusCode);
        JsonObject json = new JsonObject();
        json.addProperty("error", errorMessage);
        try {
            resp.getWriter().write(gson.toJson(json));
        } catch (IOException e) {
            logger.fatal("Unable to respond", e);
        }
    }

    @Activate
    void activate(Map<String, Object> properties) {
        modified(properties);
        logger.info("Galasa CPS API activated");
    }

    @Modified
    void modified(Map<String, Object> properties) {
        // TODO set the JWT signing key etc
    }

    @Deactivate
    void deactivate() {
        // TODO Clear the properties to prevent JWT generation
    }

}