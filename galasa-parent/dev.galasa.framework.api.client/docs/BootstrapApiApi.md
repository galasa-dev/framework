# BootstrapApiApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getEcosystemBootstrap**](BootstrapApiApi.md#getEcosystemBootstrap) | **GET** /bootstrap | Contact the Galasa ecosystem |


<a name="getEcosystemBootstrap"></a>
# **getEcosystemBootstrap**
> String getEcosystemBootstrap()

Contact the Galasa ecosystem

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.BootstrapApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    BootstrapApiApi apiInstance = new BootstrapApiApi(defaultClient);
    try {
      String result = apiInstance.getEcosystemBootstrap();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling BootstrapApiApi#getEcosystemBootstrap");
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

**String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain, application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | The Galasa ecosystem bootstrap. |  -  |
| **500** | Error |  -  |

