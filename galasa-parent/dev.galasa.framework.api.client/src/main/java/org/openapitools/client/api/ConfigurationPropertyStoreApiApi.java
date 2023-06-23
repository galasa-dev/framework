/*
 * Galasa Ecosystem API
 * The Galasa Ecosystem REST API allows you to interact with a Galasa Ecosystem.
 *
 * The version of the OpenAPI document: 0.28.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.openapitools.client.api;

import org.openapitools.client.ApiCallback;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.Configuration;
import org.openapitools.client.Pair;
import org.openapitools.client.ProgressRequestBody;
import org.openapitools.client.ProgressResponseBody;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;


import org.openapitools.client.model.CpsProperty;
import org.openapitools.client.model.JsonError;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.GenericType;

public class ConfigurationPropertyStoreApiApi {
    private ApiClient localVarApiClient;
    private int localHostIndex;
    private String localCustomBaseUrl;

    public ConfigurationPropertyStoreApiApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ConfigurationPropertyStoreApiApi(ApiClient apiClient) {
        this.localVarApiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return localVarApiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.localVarApiClient = apiClient;
    }

    public int getHostIndex() {
        return localHostIndex;
    }

    public void setHostIndex(int hostIndex) {
        this.localHostIndex = hostIndex;
    }

    public String getCustomBaseUrl() {
        return localCustomBaseUrl;
    }

    public void setCustomBaseUrl(String customBaseUrl) {
        this.localCustomBaseUrl = customBaseUrl;
    }

    /**
     * Build call for getCpsNamespaceCascadeProperty
     * @param namespace Property Namespace. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; or &#39;0&#39;-&#39;9&#39;  (required)
     * @param prefix Property Prefix. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges,   and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (required)
     * @param suffix Property suffix. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (required)
     * @param infixes Property infixes. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (optional)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> CPS Property </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Error </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call getCpsNamespaceCascadePropertyCall(String namespace, String prefix, String suffix, List<String> infixes, final ApiCallback _callback) throws ApiException {
        String basePath = null;
        // Operation Servers
        String[] localBasePaths = new String[] {  };

        // Determine Base Path to Use
        if (localCustomBaseUrl != null){
            basePath = localCustomBaseUrl;
        } else if ( localBasePaths.length > 0 ) {
            basePath = localBasePaths[localHostIndex];
        } else {
            basePath = null;
        }

        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/cps/namespace/{namespace}/prefix/{prefix}/suffix/{suffix}"
            .replaceAll("\\{" + "namespace" + "\\}", localVarApiClient.escapeString(namespace.toString()))
            .replaceAll("\\{" + "prefix" + "\\}", localVarApiClient.escapeString(prefix.toString()))
            .replaceAll("\\{" + "suffix" + "\\}", localVarApiClient.escapeString(suffix.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        if (infixes != null) {
            localVarCollectionQueryParams.addAll(localVarApiClient.parameterToPairs("multi", "infixes", infixes));
        }

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
            
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] {  };
        return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call getCpsNamespaceCascadePropertyValidateBeforeCall(String namespace, String prefix, String suffix, List<String> infixes, final ApiCallback _callback) throws ApiException {
        
        // verify the required parameter 'namespace' is set
        if (namespace == null) {
            throw new ApiException("Missing the required parameter 'namespace' when calling getCpsNamespaceCascadeProperty(Async)");
        }
        
        // verify the required parameter 'prefix' is set
        if (prefix == null) {
            throw new ApiException("Missing the required parameter 'prefix' when calling getCpsNamespaceCascadeProperty(Async)");
        }
        
        // verify the required parameter 'suffix' is set
        if (suffix == null) {
            throw new ApiException("Missing the required parameter 'suffix' when calling getCpsNamespaceCascadeProperty(Async)");
        }
        

        okhttp3.Call localVarCall = getCpsNamespaceCascadePropertyCall(namespace, prefix, suffix, infixes, _callback);
        return localVarCall;

    }

    /**
     * Get cascade CPS property
     * 
     * @param namespace Property Namespace. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; or &#39;0&#39;-&#39;9&#39;  (required)
     * @param prefix Property Prefix. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges,   and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (required)
     * @param suffix Property suffix. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (required)
     * @param infixes Property infixes. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (optional)
     * @return CpsProperty
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> CPS Property </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Error </td><td>  -  </td></tr>
     </table>
     */
    public CpsProperty getCpsNamespaceCascadeProperty(String namespace, String prefix, String suffix, List<String> infixes) throws ApiException {
        ApiResponse<CpsProperty> localVarResp = getCpsNamespaceCascadePropertyWithHttpInfo(namespace, prefix, suffix, infixes);
        return localVarResp.getData();
    }

    /**
     * Get cascade CPS property
     * 
     * @param namespace Property Namespace. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; or &#39;0&#39;-&#39;9&#39;  (required)
     * @param prefix Property Prefix. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges,   and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (required)
     * @param suffix Property suffix. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (required)
     * @param infixes Property infixes. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (optional)
     * @return ApiResponse&lt;CpsProperty&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> CPS Property </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Error </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<CpsProperty> getCpsNamespaceCascadePropertyWithHttpInfo(String namespace, String prefix, String suffix, List<String> infixes) throws ApiException {
        okhttp3.Call localVarCall = getCpsNamespaceCascadePropertyValidateBeforeCall(namespace, prefix, suffix, infixes, null);
        Type localVarReturnType = new TypeToken<CpsProperty>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Get cascade CPS property (asynchronously)
     * 
     * @param namespace Property Namespace. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; or &#39;0&#39;-&#39;9&#39;  (required)
     * @param prefix Property Prefix. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges,   and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (required)
     * @param suffix Property suffix. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (required)
     * @param infixes Property infixes. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (optional)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> CPS Property </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Error </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call getCpsNamespaceCascadePropertyAsync(String namespace, String prefix, String suffix, List<String> infixes, final ApiCallback<CpsProperty> _callback) throws ApiException {

        okhttp3.Call localVarCall = getCpsNamespaceCascadePropertyValidateBeforeCall(namespace, prefix, suffix, infixes, _callback);
        Type localVarReturnType = new TypeToken<CpsProperty>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
    /**
     * Build call for getCpsNamespaceProperties
     * @param namespace Property Namespace. First character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; or &#39;0&#39;-&#39;9&#39;  (required)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Array of CPS Properties </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> The namespace/prefix/suffix uses invalid characters or is badly formed. </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal Server Error </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call getCpsNamespacePropertiesCall(String namespace, final ApiCallback _callback) throws ApiException {
        String basePath = null;
        // Operation Servers
        String[] localBasePaths = new String[] {  };

        // Determine Base Path to Use
        if (localCustomBaseUrl != null){
            basePath = localCustomBaseUrl;
        } else if ( localBasePaths.length > 0 ) {
            basePath = localBasePaths[localHostIndex];
        } else {
            basePath = null;
        }

        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/cps/namespace/{namespace}"
            .replaceAll("\\{" + "namespace" + "\\}", localVarApiClient.escapeString(namespace.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
            
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] {  };
        return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call getCpsNamespacePropertiesValidateBeforeCall(String namespace, final ApiCallback _callback) throws ApiException {
        
        // verify the required parameter 'namespace' is set
        if (namespace == null) {
            throw new ApiException("Missing the required parameter 'namespace' when calling getCpsNamespaceProperties(Async)");
        }
        

        okhttp3.Call localVarCall = getCpsNamespacePropertiesCall(namespace, _callback);
        return localVarCall;

    }

    /**
     * Get all properties for a namepace
     * 
     * @param namespace Property Namespace. First character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; or &#39;0&#39;-&#39;9&#39;  (required)
     * @return List&lt;CpsProperty&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Array of CPS Properties </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> The namespace/prefix/suffix uses invalid characters or is badly formed. </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal Server Error </td><td>  -  </td></tr>
     </table>
     */
    public List<CpsProperty> getCpsNamespaceProperties(String namespace) throws ApiException {
        ApiResponse<List<CpsProperty>> localVarResp = getCpsNamespacePropertiesWithHttpInfo(namespace);
        return localVarResp.getData();
    }

    /**
     * Get all properties for a namepace
     * 
     * @param namespace Property Namespace. First character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; or &#39;0&#39;-&#39;9&#39;  (required)
     * @return ApiResponse&lt;List&lt;CpsProperty&gt;&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Array of CPS Properties </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> The namespace/prefix/suffix uses invalid characters or is badly formed. </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal Server Error </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<List<CpsProperty>> getCpsNamespacePropertiesWithHttpInfo(String namespace) throws ApiException {
        okhttp3.Call localVarCall = getCpsNamespacePropertiesValidateBeforeCall(namespace, null);
        Type localVarReturnType = new TypeToken<List<CpsProperty>>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Get all properties for a namepace (asynchronously)
     * 
     * @param namespace Property Namespace. First character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; or &#39;0&#39;-&#39;9&#39;  (required)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Array of CPS Properties </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> The namespace/prefix/suffix uses invalid characters or is badly formed. </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal Server Error </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call getCpsNamespacePropertiesAsync(String namespace, final ApiCallback<List<CpsProperty>> _callback) throws ApiException {

        okhttp3.Call localVarCall = getCpsNamespacePropertiesValidateBeforeCall(namespace, _callback);
        Type localVarReturnType = new TypeToken<List<CpsProperty>>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
    /**
     * Build call for getCpsNamespaces
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Array of CPS Namespaces </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal Server Error </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call getCpsNamespacesCall(final ApiCallback _callback) throws ApiException {
        String basePath = null;
        // Operation Servers
        String[] localBasePaths = new String[] {  };

        // Determine Base Path to Use
        if (localCustomBaseUrl != null){
            basePath = localCustomBaseUrl;
        } else if ( localBasePaths.length > 0 ) {
            basePath = localBasePaths[localHostIndex];
        } else {
            basePath = null;
        }

        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/cps/namespace";

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
            
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] {  };
        return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call getCpsNamespacesValidateBeforeCall(final ApiCallback _callback) throws ApiException {
        

        okhttp3.Call localVarCall = getCpsNamespacesCall(_callback);
        return localVarCall;

    }

    /**
     * Get CPS Namespaces
     * 
     * @return List&lt;String&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Array of CPS Namespaces </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal Server Error </td><td>  -  </td></tr>
     </table>
     */
    public List<String> getCpsNamespaces() throws ApiException {
        ApiResponse<List<String>> localVarResp = getCpsNamespacesWithHttpInfo();
        return localVarResp.getData();
    }

    /**
     * Get CPS Namespaces
     * 
     * @return ApiResponse&lt;List&lt;String&gt;&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Array of CPS Namespaces </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal Server Error </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<List<String>> getCpsNamespacesWithHttpInfo() throws ApiException {
        okhttp3.Call localVarCall = getCpsNamespacesValidateBeforeCall(null);
        Type localVarReturnType = new TypeToken<List<String>>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Get CPS Namespaces (asynchronously)
     * 
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Array of CPS Namespaces </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Internal Server Error </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call getCpsNamespacesAsync(final ApiCallback<List<String>> _callback) throws ApiException {

        okhttp3.Call localVarCall = getCpsNamespacesValidateBeforeCall(_callback);
        Type localVarReturnType = new TypeToken<List<String>>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
    /**
     * Build call for putCpsNamespaceProperty
     * @param namespace Property Namespace. First character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; or &#39;0&#39;-&#39;9&#39;  (required)
     * @param property Property Name.  The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (required)
     * @param cpsProperty  (required)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> CPS Property </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Error </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call putCpsNamespacePropertyCall(String namespace, String property, CpsProperty cpsProperty, final ApiCallback _callback) throws ApiException {
        String basePath = null;
        // Operation Servers
        String[] localBasePaths = new String[] {  };

        // Determine Base Path to Use
        if (localCustomBaseUrl != null){
            basePath = localCustomBaseUrl;
        } else if ( localBasePaths.length > 0 ) {
            basePath = localBasePaths[localHostIndex];
        } else {
            basePath = null;
        }

        Object localVarPostBody = cpsProperty;

        // create path and map variables
        String localVarPath = "/cps/namespace/{namespace}/property/{property}"
            .replaceAll("\\{" + "namespace" + "\\}", localVarApiClient.escapeString(namespace.toString()))
            .replaceAll("\\{" + "property" + "\\}", localVarApiClient.escapeString(property.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
            "application/json"
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] {  };
        return localVarApiClient.buildCall(basePath, localVarPath, "PUT", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call putCpsNamespacePropertyValidateBeforeCall(String namespace, String property, CpsProperty cpsProperty, final ApiCallback _callback) throws ApiException {
        
        // verify the required parameter 'namespace' is set
        if (namespace == null) {
            throw new ApiException("Missing the required parameter 'namespace' when calling putCpsNamespaceProperty(Async)");
        }
        
        // verify the required parameter 'property' is set
        if (property == null) {
            throw new ApiException("Missing the required parameter 'property' when calling putCpsNamespaceProperty(Async)");
        }
        
        // verify the required parameter 'cpsProperty' is set
        if (cpsProperty == null) {
            throw new ApiException("Missing the required parameter 'cpsProperty' when calling putCpsNamespaceProperty(Async)");
        }
        

        okhttp3.Call localVarCall = putCpsNamespacePropertyCall(namespace, property, cpsProperty, _callback);
        return localVarCall;

    }

    /**
     * Put new CPS Property
     * Searches multiple places in the property store for the first property matching the namespace,  prefix and suffix, and as many of the leading infix strings as possible. This results in a value which is the most specific, given a sparsely populated hierarchical  structure of property names. Over-rides of values (if present) are returned in preference to the normal stored value  of a property. 
     * @param namespace Property Namespace. First character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; or &#39;0&#39;-&#39;9&#39;  (required)
     * @param property Property Name.  The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (required)
     * @param cpsProperty  (required)
     * @return CpsProperty
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> CPS Property </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Error </td><td>  -  </td></tr>
     </table>
     */
    public CpsProperty putCpsNamespaceProperty(String namespace, String property, CpsProperty cpsProperty) throws ApiException {
        ApiResponse<CpsProperty> localVarResp = putCpsNamespacePropertyWithHttpInfo(namespace, property, cpsProperty);
        return localVarResp.getData();
    }

    /**
     * Put new CPS Property
     * Searches multiple places in the property store for the first property matching the namespace,  prefix and suffix, and as many of the leading infix strings as possible. This results in a value which is the most specific, given a sparsely populated hierarchical  structure of property names. Over-rides of values (if present) are returned in preference to the normal stored value  of a property. 
     * @param namespace Property Namespace. First character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; or &#39;0&#39;-&#39;9&#39;  (required)
     * @param property Property Name.  The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (required)
     * @param cpsProperty  (required)
     * @return ApiResponse&lt;CpsProperty&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> CPS Property </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Error </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<CpsProperty> putCpsNamespacePropertyWithHttpInfo(String namespace, String property, CpsProperty cpsProperty) throws ApiException {
        okhttp3.Call localVarCall = putCpsNamespacePropertyValidateBeforeCall(namespace, property, cpsProperty, null);
        Type localVarReturnType = new TypeToken<CpsProperty>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Put new CPS Property (asynchronously)
     * Searches multiple places in the property store for the first property matching the namespace,  prefix and suffix, and as many of the leading infix strings as possible. This results in a value which is the most specific, given a sparsely populated hierarchical  structure of property names. Over-rides of values (if present) are returned in preference to the normal stored value  of a property. 
     * @param namespace Property Namespace. First character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; or &#39;0&#39;-&#39;9&#39;  (required)
     * @param property Property Name.  The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  (required)
     * @param cpsProperty  (required)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> CPS Property </td><td>  -  </td></tr>
        <tr><td> 500 </td><td> Error </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call putCpsNamespacePropertyAsync(String namespace, String property, CpsProperty cpsProperty, final ApiCallback<CpsProperty> _callback) throws ApiException {

        okhttp3.Call localVarCall = putCpsNamespacePropertyValidateBeforeCall(namespace, property, cpsProperty, _callback);
        Type localVarReturnType = new TypeToken<CpsProperty>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
}
