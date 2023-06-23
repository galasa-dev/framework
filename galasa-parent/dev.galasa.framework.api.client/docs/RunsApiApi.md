# RunsApiApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getRunsGroup**](RunsApiApi.md#getRunsGroup) | **GET** /runs/{groupId} | Get group runs |
| [**postSubmitTestRuns**](RunsApiApi.md#postSubmitTestRuns) | **POST** /runs/{groupId} | Sumbit test runs |


<a name="getRunsGroup"></a>
# **getRunsGroup**
> TestRuns getRunsGroup(groupId)

Get group runs

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.RunsApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    RunsApiApi apiInstance = new RunsApiApi(defaultClient);
    String groupId = "groupId_example"; // String | Run Group ID
    try {
      TestRuns result = apiInstance.getRunsGroup(groupId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling RunsApiApi#getRunsGroup");
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
| **groupId** | **String**| Run Group ID | |

### Return type

[**TestRuns**](TestRuns.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Run Info |  -  |

<a name="postSubmitTestRuns"></a>
# **postSubmitTestRuns**
> TestRuns postSubmitTestRuns(groupId, testRunRequest)

Sumbit test runs

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.RunsApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    RunsApiApi apiInstance = new RunsApiApi(defaultClient);
    String groupId = "groupId_example"; // String | Run Group ID
    TestRunRequest testRunRequest = new TestRunRequest(); // TestRunRequest | 
    try {
      TestRuns result = apiInstance.postSubmitTestRuns(groupId, testRunRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling RunsApiApi#postSubmitTestRuns");
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
| **groupId** | **String**| Run Group ID | |
| **testRunRequest** | [**TestRunRequest**](TestRunRequest.md)|  | |

### Return type

[**TestRuns**](TestRuns.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Test Submitted |  -  |

