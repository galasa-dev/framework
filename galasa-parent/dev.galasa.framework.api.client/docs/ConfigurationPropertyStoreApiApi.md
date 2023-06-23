# ConfigurationPropertyStoreApiApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getCpsNamespaceCascadeProperty**](ConfigurationPropertyStoreApiApi.md#getCpsNamespaceCascadeProperty) | **GET** /cps/namespace/{namespace}/prefix/{prefix}/suffix/{suffix} | Get cascade CPS property |
| [**getCpsNamespaceProperties**](ConfigurationPropertyStoreApiApi.md#getCpsNamespaceProperties) | **GET** /cps/namespace/{namespace} | Get all properties for a namepace |
| [**getCpsNamespaces**](ConfigurationPropertyStoreApiApi.md#getCpsNamespaces) | **GET** /cps/namespace | Get CPS Namespaces |
| [**putCpsNamespaceProperty**](ConfigurationPropertyStoreApiApi.md#putCpsNamespaceProperty) | **PUT** /cps/namespace/{namespace}/property/{property} | Put new CPS Property |


<a name="getCpsNamespaceCascadeProperty"></a>
# **getCpsNamespaceCascadeProperty**
> CpsProperty getCpsNamespaceCascadeProperty(namespace, prefix, suffix, infixes)

Get cascade CPS property

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ConfigurationPropertyStoreApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ConfigurationPropertyStoreApiApi apiInstance = new ConfigurationPropertyStoreApiApi(defaultClient);
    String namespace = "namespace_example"; // String | Property Namespace. The first character of the namespace must be in the 'a'-'z' or 'A'-'Z' ranges, and following characters can be 'a'-'z' or 'A'-'Z' or '0'-'9' 
    String prefix = "prefix_example"; // String | Property Prefix. The first character of the namespace must be in the 'a'-'z' or 'A'-'Z' ranges,   and following characters can be 'a'-'z', 'A'-'Z', '0'-'9', '.' (period), '-' (dash) or '_' (underscore) 
    String suffix = "suffix_example"; // String | Property suffix. The first character of the namespace must be in the 'a'-'z' or 'A'-'Z' ranges, and following characters can be 'a'-'z', 'A'-'Z', '0'-'9', '.' (period), '-' (dash) or '_' (underscore) 
    List<String> infixes = Arrays.asList(); // List<String> | Property infixes. The first character of the namespace must be in the 'a'-'z' or 'A'-'Z' ranges, and following characters can be 'a'-'z', 'A'-'Z', '0'-'9', '.' (period), '-' (dash) or '_' (underscore) 
    try {
      CpsProperty result = apiInstance.getCpsNamespaceCascadeProperty(namespace, prefix, suffix, infixes);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ConfigurationPropertyStoreApiApi#getCpsNamespaceCascadeProperty");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **namespace** | **String**| Property Namespace. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; or &#39;0&#39;-&#39;9&#39;  | |
| **prefix** | **String**| Property Prefix. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges,   and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  | |
| **suffix** | **String**| Property suffix. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  | |
| **infixes** | [**List&lt;String&gt;**](String.md)| Property infixes. The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  | [optional] |

### Return type

[**CpsProperty**](CpsProperty.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | CPS Property |  -  |
| **500** | Error |  -  |

<a name="getCpsNamespaceProperties"></a>
# **getCpsNamespaceProperties**
> List&lt;CpsProperty&gt; getCpsNamespaceProperties(namespace)

Get all properties for a namepace

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ConfigurationPropertyStoreApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ConfigurationPropertyStoreApiApi apiInstance = new ConfigurationPropertyStoreApiApi(defaultClient);
    String namespace = "namespace_example"; // String | Property Namespace. First character of the namespace must be in the 'a'-'z' or 'A'-'Z' ranges, and following characters can be 'a'-'z' or 'A'-'Z' or '0'-'9' 
    try {
      List<CpsProperty> result = apiInstance.getCpsNamespaceProperties(namespace);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ConfigurationPropertyStoreApiApi#getCpsNamespaceProperties");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **namespace** | **String**| Property Namespace. First character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; or &#39;0&#39;-&#39;9&#39;  | |

### Return type

[**List&lt;CpsProperty&gt;**](CpsProperty.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Array of CPS Properties |  -  |
| **400** | The namespace/prefix/suffix uses invalid characters or is badly formed. |  -  |
| **500** | Internal Server Error |  -  |

<a name="getCpsNamespaces"></a>
# **getCpsNamespaces**
> List&lt;String&gt; getCpsNamespaces()

Get CPS Namespaces

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ConfigurationPropertyStoreApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ConfigurationPropertyStoreApiApi apiInstance = new ConfigurationPropertyStoreApiApi(defaultClient);
    try {
      List<String> result = apiInstance.getCpsNamespaces();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ConfigurationPropertyStoreApiApi#getCpsNamespaces");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**List&lt;String&gt;**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Array of CPS Namespaces |  -  |
| **500** | Internal Server Error |  -  |

<a name="putCpsNamespaceProperty"></a>
# **putCpsNamespaceProperty**
> CpsProperty putCpsNamespaceProperty(namespace, property, cpsProperty)

Put new CPS Property

Searches multiple places in the property store for the first property matching the namespace,  prefix and suffix, and as many of the leading infix strings as possible. This results in a value which is the most specific, given a sparsely populated hierarchical  structure of property names. Over-rides of values (if present) are returned in preference to the normal stored value  of a property. 

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ConfigurationPropertyStoreApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ConfigurationPropertyStoreApiApi apiInstance = new ConfigurationPropertyStoreApiApi(defaultClient);
    String namespace = "namespace_example"; // String | Property Namespace. First character of the namespace must be in the 'a'-'z' or 'A'-'Z' ranges, and following characters can be 'a'-'z' or 'A'-'Z' or '0'-'9' 
    String property = "property_example"; // String | Property Name.  The first character of the namespace must be in the 'a'-'z' or 'A'-'Z' ranges, and following characters can be 'a'-'z', 'A'-'Z', '0'-'9', '.' (period), '-' (dash) or '_' (underscore) 
    CpsProperty cpsProperty = new CpsProperty(); // CpsProperty | 
    try {
      CpsProperty result = apiInstance.putCpsNamespaceProperty(namespace, property, cpsProperty);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ConfigurationPropertyStoreApiApi#putCpsNamespaceProperty");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **namespace** | **String**| Property Namespace. First character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; or &#39;0&#39;-&#39;9&#39;  | |
| **property** | **String**| Property Name.  The first character of the namespace must be in the &#39;a&#39;-&#39;z&#39; or &#39;A&#39;-&#39;Z&#39; ranges, and following characters can be &#39;a&#39;-&#39;z&#39;, &#39;A&#39;-&#39;Z&#39;, &#39;0&#39;-&#39;9&#39;, &#39;.&#39; (period), &#39;-&#39; (dash) or &#39;_&#39; (underscore)  | |
| **cpsProperty** | [**CpsProperty**](CpsProperty.md)|  | |

### Return type

[**CpsProperty**](CpsProperty.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | CPS Property |  -  |
| **500** | Error |  -  |

