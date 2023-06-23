# ResultArchiveStoreApiApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getRasRequestors**](ResultArchiveStoreApiApi.md#getRasRequestors) | **GET** /ras/requestors | Get all known requestors |
| [**getRasResultNames**](ResultArchiveStoreApiApi.md#getRasResultNames) | **GET** /ras/resultnames | Get all the known result names |
| [**getRasRunArtifactByPath**](ResultArchiveStoreApiApi.md#getRasRunArtifactByPath) | **GET** /ras/runs/{runid}/files/{artifactPath} | Download Artifact for a given runid by artifactPath |
| [**getRasRunArtifactList**](ResultArchiveStoreApiApi.md#getRasRunArtifactList) | **GET** /ras/runs/{runid}/artifacts | Get the available Run artifacts which can be downloaded. |
| [**getRasRunById**](ResultArchiveStoreApiApi.md#getRasRunById) | **GET** /ras/runs/{runid} | Get Run by ID |
| [**getRasRunLog**](ResultArchiveStoreApiApi.md#getRasRunLog) | **GET** /ras/runs/{runid}/runlog | Get Run Log |
| [**getRasSearchRuns**](ResultArchiveStoreApiApi.md#getRasSearchRuns) | **GET** /ras/runs | Get Runs from Query |
| [**getRasTestclasses**](ResultArchiveStoreApiApi.md#getRasTestclasses) | **GET** /ras/testclasses | Get all the known test classes |


<a name="getRasRequestors"></a>
# **getRasRequestors**
> Requestors getRasRequestors(sort)

Get all known requestors

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ResultArchiveStoreApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ResultArchiveStoreApiApi apiInstance = new ResultArchiveStoreApiApi(defaultClient);
    String sort = "sort_example"; // String | provides sorting, requestor:asc or requestor:desc
    try {
      Requestors result = apiInstance.getRasRequestors(sort);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ResultArchiveStoreApiApi#getRasRequestors");
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
| **sort** | **String**| provides sorting, requestor:asc or requestor:desc | [optional] |

### Return type

[**Requestors**](Requestors.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Requestors |  -  |
| **500** | Error |  -  |

<a name="getRasResultNames"></a>
# **getRasResultNames**
> ResultNames getRasResultNames(sort)

Get all the known result names

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ResultArchiveStoreApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ResultArchiveStoreApiApi apiInstance = new ResultArchiveStoreApiApi(defaultClient);
    String sort = "sort_example"; // String | provides sorting, results:asc or results:desc
    try {
      ResultNames result = apiInstance.getRasResultNames(sort);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ResultArchiveStoreApiApi#getRasResultNames");
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
| **sort** | **String**| provides sorting, results:asc or results:desc | [optional] |

### Return type

[**ResultNames**](ResultNames.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | ResultNames |  -  |
| **500** | Error |  -  |

<a name="getRasRunArtifactByPath"></a>
# **getRasRunArtifactByPath**
> File getRasRunArtifactByPath(runid, artifactPath)

Download Artifact for a given runid by artifactPath

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ResultArchiveStoreApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ResultArchiveStoreApiApi apiInstance = new ResultArchiveStoreApiApi(defaultClient);
    String runid = "runid_example"; // String | Run Id
    String artifactPath = "artifactPath_example"; // String | Run Artifact path
    try {
      File result = apiInstance.getRasRunArtifactByPath(runid, artifactPath);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ResultArchiveStoreApiApi#getRasRunArtifactByPath");
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
| **runid** | **String**| Run Id | |
| **artifactPath** | **String**| Run Artifact path | |

### Return type

[**File**](File.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/octet-stream, application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | The artifact is made available to read. |  -  |
| **404** | Not Found |  -  |
| **500** | Internal Server |  -  |

<a name="getRasRunArtifactList"></a>
# **getRasRunArtifactList**
> Set&lt;ArtifactIndexEntry&gt; getRasRunArtifactList(runid)

Get the available Run artifacts which can be downloaded.

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ResultArchiveStoreApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ResultArchiveStoreApiApi apiInstance = new ResultArchiveStoreApiApi(defaultClient);
    String runid = "runid_example"; // String | Run Id
    try {
      Set<ArtifactIndexEntry> result = apiInstance.getRasRunArtifactList(runid);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ResultArchiveStoreApiApi#getRasRunArtifactList");
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
| **runid** | **String**| Run Id | |

### Return type

[**Set&lt;ArtifactIndexEntry&gt;**](ArtifactIndexEntry.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | The list of artifacts was returned. |  -  |
| **404** | Not Found |  -  |
| **500** | Internal Server Error |  -  |

<a name="getRasRunById"></a>
# **getRasRunById**
> Run getRasRunById(runid)

Get Run by ID

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ResultArchiveStoreApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ResultArchiveStoreApiApi apiInstance = new ResultArchiveStoreApiApi(defaultClient);
    String runid = "runid_example"; // String | Run Id
    try {
      Run result = apiInstance.getRasRunById(runid);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ResultArchiveStoreApiApi#getRasRunById");
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
| **runid** | **String**| Run Id | |

### Return type

[**Run**](Run.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Run Data |  -  |
| **500** | Error |  -  |

<a name="getRasRunLog"></a>
# **getRasRunLog**
> String getRasRunLog(runid)

Get Run Log

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ResultArchiveStoreApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ResultArchiveStoreApiApi apiInstance = new ResultArchiveStoreApiApi(defaultClient);
    String runid = "runid_example"; // String | Run Id
    try {
      String result = apiInstance.getRasRunLog(runid);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ResultArchiveStoreApiApi#getRasRunLog");
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
| **runid** | **String**| Run Id | |

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
| **200** | Run Data |  -  |
| **500** | Error |  -  |

<a name="getRasSearchRuns"></a>
# **getRasSearchRuns**
> RunResults getRasSearchRuns(sort, result, bundle, requestor, from, to, testname, page, size, runId, runname)

Get Runs from Query

API endpoint to query the Result Archive Store (RAS) for a (possibly sorted)  list of runs based on the given search criteria.  The results returned are paginated, in that the number of desired records per page can be  set, and if there are more test run records to retrieve, requests can be made for  successive pages of results using the same query parameters, but varying the &#39;page&#39; value.  Note: When querying multiple pages of results, tests may complete, or be started between  successive calls to this endpoint. When the &#39;to&#39; field is not used, no timeframe  limit is specified in the query, so results retrieved in later pages may contain  test runs which were already retrieved in previous pages of the same query critera.  Invalid query parameters are ignored. For example: a &#39;cache-buster&#39; parameter. 

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ResultArchiveStoreApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ResultArchiveStoreApiApi apiInstance = new ResultArchiveStoreApiApi(defaultClient);
    String sort = "result:asc"; // String | Sorts the returned runs based on the sort field.  Supports sorting fields 'to','result' and 'testclass'.  Use '{FIELD-NAME}:asc' to sort in ascending order. Use '{FIELD-NAME}:desc' to sort in descending order.  You can use multiple instances of this query parameter, or specify multiple sort orders using one query parameter, and a comma-separated  list of sort orders. 
    String result = "Passed"; // String | Result Status for the run. Commonly queried values: 'EnvFail','Passed','Failed' 
    String bundle = "dev.galasa.inttests"; // String | The name of the OSGi bundle that the desired test run(s) were loaded with. 
    String requestor = "MyAutomationJobName"; // String | Name of the test requestor / submitter
    OffsetDateTime from = OffsetDateTime.parse("2023-04-11T09:42:06.589180Z"); // OffsetDateTime | Retrieve runs that started at a time after this date and time.  The only scenario in which from can be omitted is when a runname has been supplied 
    OffsetDateTime to = OffsetDateTime.parse("2023-04-11T09:43:27.324075Z""); // OffsetDateTime | Retrieve runs that ended at a date and time prior to this date and time value. If you specify this parameter, only test runs which have completed will be returned.  Tests currently in-flight will not be visible. 
    String testname = "dev.galasa.inttests.simbank.local.mvp.SimBankLocalJava11UbuntuMvp"; // String | The full test name (package + short test name)
    Integer page = 2; // Integer | Causes a specific page in the available results to be returned.  The first page is page 1. If omitted, then page 1 is returned. 
    Integer size = 20; // Integer | The number of test results returned within each page. If omitted, the default value is 100. 
    String runId = "cdb-a4ddb7fd1dab8d6025e4f3894010d20d"; // String | The ID for a specific test run as seen by the RAS.  This number is unique across the system, so using this field you can expect one or zero test runs in the first page. 
    String runname = "U1578"; // String | The name of the test run for which details will be returned. It will normally be unique, but this is not guaranteed, so you may see multiple results for the same runname under some rare circumstances. 
    try {
      RunResults result = apiInstance.getRasSearchRuns(sort, result, bundle, requestor, from, to, testname, page, size, runId, runname);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ResultArchiveStoreApiApi#getRasSearchRuns");
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
| **sort** | **String**| Sorts the returned runs based on the sort field.  Supports sorting fields &#39;to&#39;,&#39;result&#39; and &#39;testclass&#39;.  Use &#39;{FIELD-NAME}:asc&#39; to sort in ascending order. Use &#39;{FIELD-NAME}:desc&#39; to sort in descending order.  You can use multiple instances of this query parameter, or specify multiple sort orders using one query parameter, and a comma-separated  list of sort orders.  | |
| **result** | **String**| Result Status for the run. Commonly queried values: &#39;EnvFail&#39;,&#39;Passed&#39;,&#39;Failed&#39;  | [optional] |
| **bundle** | **String**| The name of the OSGi bundle that the desired test run(s) were loaded with.  | [optional] |
| **requestor** | **String**| Name of the test requestor / submitter | [optional] |
| **from** | **OffsetDateTime**| Retrieve runs that started at a time after this date and time.  The only scenario in which from can be omitted is when a runname has been supplied  | [optional] |
| **to** | **OffsetDateTime**| Retrieve runs that ended at a date and time prior to this date and time value. If you specify this parameter, only test runs which have completed will be returned.  Tests currently in-flight will not be visible.  | [optional] |
| **testname** | **String**| The full test name (package + short test name) | [optional] |
| **page** | **Integer**| Causes a specific page in the available results to be returned.  The first page is page 1. If omitted, then page 1 is returned.  | [optional] |
| **size** | **Integer**| The number of test results returned within each page. If omitted, the default value is 100.  | [optional] |
| **runId** | **String**| The ID for a specific test run as seen by the RAS.  This number is unique across the system, so using this field you can expect one or zero test runs in the first page.  | [optional] |
| **runname** | **String**| The name of the test run for which details will be returned. It will normally be unique, but this is not guaranteed, so you may see multiple results for the same runname under some rare circumstances.  | [optional] |

### Return type

[**RunResults**](RunResults.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Array of Run Objects |  -  |
| **400** | Bad Request |  -  |
| **404** | Not Found |  -  |
| **500** | Internal Server |  -  |

<a name="getRasTestclasses"></a>
# **getRasTestclasses**
> TestClasses getRasTestclasses(sort)

Get all the known test classes

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ResultArchiveStoreApiApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    ResultArchiveStoreApiApi apiInstance = new ResultArchiveStoreApiApi(defaultClient);
    String sort = "sort_example"; // String | Provide Sorting
    try {
      TestClasses result = apiInstance.getRasTestclasses(sort);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ResultArchiveStoreApiApi#getRasTestclasses");
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
| **sort** | **String**| Provide Sorting | [optional] |

### Return type

[**TestClasses**](TestClasses.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | TestClasses |  -  |
| **500** | Error |  -  |

