# WebUiWorklistApiApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**addWebuiWorklistRunId**](WebUiWorklistApiApi.md#addWebuiWorklistRunId) | **PUT** /webui/worklist | Add RAS runId to Worklist |
| [**deleteWebuiWorklistRunId**](WebUiWorklistApiApi.md#deleteWebuiWorklistRunId) | **DELETE** /webui/worklist | Remove RAS runId from Worklist |
| [**getWebuiWorklist**](WebUiWorklistApiApi.md#getWebuiWorklist) | **GET** /webui/worklist | Get the users Worklist |


<a name="addWebuiWorklistRunId"></a>
# **addWebuiWorklistRunId**
> Worklist addWebuiWorklistRunId(runId)

Add RAS runId to Worklist

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.WebUiWorklistApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    WebUiWorklistApiApi apiInstance = new WebUiWorklistApiApi(defaultClient);
    String runId = "runId_example"; // String | Run ID
    try {
      Worklist result = apiInstance.addWebuiWorklistRunId(runId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling WebUiWorklistApiApi#addWebuiWorklistRunId");
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
| **runId** | **String**| Run ID | |

### Return type

[**Worklist**](Worklist.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Run added to Worklist and updated Worklist returned |  -  |
| **404** | Run not found |  -  |

<a name="deleteWebuiWorklistRunId"></a>
# **deleteWebuiWorklistRunId**
> Worklist deleteWebuiWorklistRunId(runId)

Remove RAS runId from Worklist

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.WebUiWorklistApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    WebUiWorklistApiApi apiInstance = new WebUiWorklistApiApi(defaultClient);
    String runId = "runId_example"; // String | Run Id
    try {
      Worklist result = apiInstance.deleteWebuiWorklistRunId(runId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling WebUiWorklistApiApi#deleteWebuiWorklistRunId");
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
| **runId** | **String**| Run Id | |

### Return type

[**Worklist**](Worklist.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Run removed from Worklist and updated Worklist returned |  -  |
| **404** | Run not found |  -  |

<a name="getWebuiWorklist"></a>
# **getWebuiWorklist**
> Worklist getWebuiWorklist()

Get the users Worklist

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.WebUiWorklistApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    WebUiWorklistApiApi apiInstance = new WebUiWorklistApiApi(defaultClient);
    try {
      Worklist result = apiInstance.getWebuiWorklist();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling WebUiWorklistApiApi#getWebuiWorklist");
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

[**Worklist**](Worklist.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Array of Worklist objects |  -  |
| **500** | Error |  -  |

