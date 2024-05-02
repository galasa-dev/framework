/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal;

import com.google.gson.stream.JsonReader;

import dev.galasa.framework.api.common.mocks.MockConfigurationPropertyStoreService;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.cps.mocks.*;
import dev.galasa.framework.spi.*;
import dev.galasa.framework.spi.utils.GalasaGson;

import org.junit.Test;
import java.text.*;

import static org.assertj.core.api.Assertions.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class AccessCpsTest {

    private final GalasaGson gson = new GalasaGson();


    /**
     * A subclass of AccessCps servlet solely so that we can set the logger into the superclass.
     * ... so we can suppress exceptions appearing in the test console, and also check the contents
     * of the log against what we expect to be there.
     */
    static class LogCapturingAccessCps extends AccessCps {
        public LogCapturingAccessCps() {
            super();
            super.logger = new MockLogger();
        }

        public MockLogger getLogger() {
            return (MockLogger) super.logger ;
        }
    }

    @Test
    public void malformedPutUrlReturnsBadRequestError() throws Exception {
        // Given...

        // A mock framework which passes back the property store.
        IFramework mockFramework = new MockFramework();

        // Inject our mock framework into the servlet.
        LogCapturingAccessCps servlet = new LogCapturingAccessCps();
        servlet.framework = mockFramework ;

        HttpServletRequest request = new MockHttpRequest("/badUrl");

        MockHttpResponse response = new MockHttpResponse();

        // When...
        servlet.doPut(request,response);

        // Then...
        assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");

        String errorMessage = response.getPayloadAsErrorMessage();
        assertThat(errorMessage).isEqualTo("Invalid PUT URL - /badUrl");
        assertThat(response.getStatus()).isEqualTo(400); // Bad request
    }

    @Test
    public void getNamespacesSecureFailureCausesErrorReportedAndExceptionIsLogged() throws Exception {

        // A mock framework which fails to pass back the property store.
        IFramework mockFramework = new MockFramework() {
            @Override
            public IConfigurationPropertyStoreService getConfigurationPropertyService(String namespace) throws ConfigurationPropertyStoreException {
                assertThat(namespace).isEqualTo("framework");
                throw new ConfigurationPropertyStoreException("Failed on purpose in a unit test");
            }
        };

        // Inject our mock framework into the servlet.
        LogCapturingAccessCps servlet = new LogCapturingAccessCps();
        servlet.framework = mockFramework ;

        HttpServletRequest request = new MockHttpRequest("");

        MockHttpResponse response = new MockHttpResponse();

        // When...
        servlet.doGet(request,response);

        // Then...
        assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(response.getStatus()).isEqualTo(400); // Expect server error.

        String errorMessage = response.getPayloadAsErrorMessage();
        assertThat(errorMessage).isEqualTo("Failed on purpose in a unit test");

        // Check that the exception was logged as an error in the log.
        LogRecord errorLogRecord = servlet.getLogger().getFirstLogRecordContainingText("Failed on purpose in a unit test");
        assertThat(errorLogRecord).isNotNull();
        assertThat(errorLogRecord.getCause()).isNotNull().isInstanceOf(ConfigurationPropertyStoreException.class);
        assertThat(errorLogRecord.getType()).isEqualTo(LogRecordType.INFO);
    }

    @Test
    public void getNamespacesDoesNotListDSSNoTrailingSlash() throws Exception {
        getNamespacesDoesNotListDSS("");
    }

    @Test
    public void getNamespacesDoesNotListDSSWithTrailingSlash() throws Exception {
        getNamespacesDoesNotListDSS("/");
    }

    public void getNamespacesDoesNotListDSS(String path) throws Exception {

        // Given...

        // We pretend some namespaces exist.
        List<String> nameSpacesWhichExist = new LinkedList<>();
        nameSpacesWhichExist.add("dss");
        nameSpacesWhichExist.add("myNameSpace");

        // A property store which can serve-up namespaces that exist.
        IConfigurationPropertyStoreService mockPropsStoreService = new MockConfigurationPropertyStoreService() {
            @Override
            public List<String> getCPSNamespaces() {
                return nameSpacesWhichExist;
            }
        };

        // A mock framework which passes back the property store.
        IFramework mockFramework = new MockFramework() {
            @Override
            public IConfigurationPropertyStoreService getConfigurationPropertyService(String namespace) throws ConfigurationPropertyStoreException {
                assertThat(namespace).isEqualTo("framework");
                return mockPropsStoreService;
            }
        };

        // Inject our mock framework into the servlet.
        LogCapturingAccessCps servlet = new LogCapturingAccessCps();
        servlet.framework = mockFramework ;

        HttpServletRequest request = new MockHttpRequest(path);

        MockHttpResponse response = new MockHttpResponse();

        // When...
        servlet.doGet(request,response);

        // Then...
        assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(response.getStatus()).isEqualTo(200);

        // The result should be an array of namespaces
        JsonReader jReader = response.getPayloadAsJsonReader();
        String[] nameSpacesArray = gson.fromJson(jReader , String[].class);

        assertThat(nameSpacesArray)
                .isNotNull().isNotEmpty()
                .containsExactly("myNameSpace")
                .doesNotContain("dss");
    }

    @Test
    public void getAllPropertiesInANamespaceNamespaceUsesInvalidCharacters() throws Exception {

        // Given...

        // A mock framework which passes back the property store.
        IFramework mockFramework = new MockFramework() {
            @Override
            public IConfigurationPropertyStoreService getConfigurationPropertyService(String namespace) throws ConfigurationPropertyStoreException {
                FrameworkException originalCause = new FrameworkException(new FrameworkErrorDetailsBase (2,
                        "bad namespace characters - for unit testing"));
                throw new ConfigurationPropertyStoreException(originalCause);
            }
        };

        // Inject our mock framework into the servlet.
        LogCapturingAccessCps servlet = new LogCapturingAccessCps();
        servlet.framework = mockFramework ;

        HttpServletRequest request = new MockHttpRequest("/namespaceUsing$invalid£characters");

        MockHttpResponse response = new MockHttpResponse();

        // When...
        servlet.doGet(request,response);

        // Then...
        assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(response.getStatus()).isEqualTo(400); // Resource not found.

        // Check that the error was good.
        String errorMessage = response.getPayloadAsErrorMessage();
        assertThat(errorMessage)
                .doesNotContain("Internal server error.")
                .contains("Invalid namespace");
    }

    @Test
    public void getUsingBadlySpeltPrefixShouldFailBadUrl() throws Exception {
        getUsingBadUrl("/myValidNamespace/prefi");
    }

    @Test
    public void getUsingBadUrlWithoutLeadingSlashShouldFailBadUrl() throws Exception {
        getUsingBadUrl("namespa");
    }

    public void getUsingBadUrl(String path) throws Exception {
        // Given...

        // Inject our mock framework into the servlet.
        AccessCps servlet = new LogCapturingAccessCps();

        HttpServletRequest request = new MockHttpRequest(path);

        MockHttpResponse response = new MockHttpResponse();

        // When...
        servlet.doGet(request,response);

        // Then...
        assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(response.getStatus()).isEqualTo(400); // Bad URL.

        // Check that the error was good.
        String errorMessage = response.getPayloadAsErrorMessage();
        assertThat(errorMessage)
                .doesNotContain("Internal server error.")
                .contains("Invalid GET URL - ")
                .contains(path);
    }

    @Test
    public void getNamespacesPropertiesDoesNotListDSSOnes() throws Exception {

        // Given...

        // A mock framework which passes back the property store.
        IFramework mockFramework = new MockFramework() {
            @Override
            public IConfigurationPropertyStoreService getConfigurationPropertyService(String namespace) throws ConfigurationPropertyStoreException {
                fail("did not expect the servlet to query the property store for the dss component");
                return null ;
            }
        };

        // Inject our mock framework into the servlet.
        LogCapturingAccessCps servlet = new LogCapturingAccessCps();
        servlet.framework = mockFramework ;

        HttpServletRequest request = new MockHttpRequest("/dss");

        MockHttpResponse response = new MockHttpResponse();

        // When...
        servlet.doGet(request,response);

        // Then...
        assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(response.getStatus()).isEqualTo(404); // Resource not found

        // Check that the error was good.
        String errorMessage = response.getPayloadAsErrorMessage();
        assertThat(errorMessage)
                .doesNotContain("Internal server error.")
                .contains("dss")
                .contains("is not found")
                ;

        // Check that the exception was logged as an error in the log.
        LogRecord infoLogRecord = servlet.getLogger().getFirstLogRecordContainingText("User tried to get properties from protected namespace");
        assertThat(infoLogRecord).isNotNull();
        assertThat(infoLogRecord.getType()).isEqualTo(LogRecordType.INFO);
    }


    @Test
    public void getNamespacesPropertiesListAllowableOnes() throws Exception {

        // Given...

        // A property store which can serve-up namespaces that exist.
        IConfigurationPropertyStoreService mockPropsStoreService = new MockConfigurationPropertyStoreService() {
            @Override
            public Map<String, String> getAllProperties() {
                return new HashMap<>() {{ put("a","aValue"); put("c","cValue"); }};
            }
        };

        // A mock framework which passes back the property store.
        IFramework mockFramework = new MockFramework() {
            @Override
            public IConfigurationPropertyStoreService getConfigurationPropertyService(String namespace) throws ConfigurationPropertyStoreException {
                assertThat(namespace).isEqualTo("notdss");
                return mockPropsStoreService ;
            }
        };

        // Inject our mock framework into the servlet.
        LogCapturingAccessCps servlet = new LogCapturingAccessCps();
        servlet.framework = mockFramework ;

        HttpServletRequest request = new MockHttpRequest("/notdss");

        MockHttpResponse response = new MockHttpResponse();

        // When...
        servlet.doGet(request,response);

        // Then...
        assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(response.getStatus()).isEqualTo(200); // OK

        // The result should be an array of namespaces
        JsonReader jReader = response.getPayloadAsJsonReader();


        NameValuePair[] propertiesArray = gson.fromJson(jReader , NameValuePair[].class);

        // Transfer values into a java map.
        Map<String,String> properties = new HashMap<>();
        for ( NameValuePair pair : propertiesArray ) {
            properties.put(pair.name, pair.value);
        }

        assertThat(properties)
                .containsKey("a")
                .containsKey("c");
        assertThat(properties.get("a")).isEqualTo("aValue");
        assertThat(properties.get("c")).isEqualTo("cValue");
    }

    public static class NameValuePair {
        String name ;
        String value ;
    }

    @Test
    public void setNamespacePropertyOK() throws Exception {

        // Given...

        // A property store which can serve-up namespaces that exist.
        IConfigurationPropertyStoreService mockPropsStoreService = new MockConfigurationPropertyStoreService() {
            @Override
            public void setProperty(String name, String value) throws ConfigurationPropertyStoreException {
                assertThat(name).isEqualTo("my.prop");
                assertThat(value).isEqualTo("myvalue");
            }
        };

        // A mock framework which passes back the property store.
        IFramework mockFramework = new MockFramework() {
            @Override
            public IConfigurationPropertyStoreService getConfigurationPropertyService(String namespace) throws ConfigurationPropertyStoreException {
                assertThat(namespace).isEqualTo("notdssbutvalid");
                return mockPropsStoreService ;
            }
        };

        // Inject our mock framework into the servlet.
        LogCapturingAccessCps servlet = new LogCapturingAccessCps();
        servlet.framework = mockFramework ;

        MockHttpRequest request = new MockHttpRequest("/notdssbutvalid/property/my.prop");
        request.setBody(
                "{\n"+
                "  \"name\":\"my.prop\",\n"+
                "  \"value\":\"myvalue\"\n"+
                "}\n");
        MockHttpResponse response = new MockHttpResponse();

        // When...
        servlet.doPut(request,response);

        // Then...
        assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(response.getStatus()).isEqualTo(200); // Unauthorised.

        // The result should be an array of namespaces
        response.getWriter().close();
        JsonReader jReader = response.getPayloadAsJsonReader();


        NameValuePair property = gson.fromJson(jReader , NameValuePair.class);

        assertThat(property.name).isEqualTo("my.prop");
        assertThat(property.value).isEqualTo("myvalue");
    }



    @Test
    public void setPropertyOnDSSFailsUnauthorised() throws Exception {

        // Given...

        // A property store which can serve-up namespaces that exist.
        IConfigurationPropertyStoreService mockPropsStoreService = new MockConfigurationPropertyStoreService() {
            @Override
            public void setProperty(String name, String value) throws ConfigurationPropertyStoreException {
                assertThat(name).isEqualTo("my.prop");
                assertThat(value).isEqualTo("myvalue");
            }
        };

        // A mock framework which passes back the property store.
        IFramework mockFramework = new MockFramework() {
            @Override
            public IConfigurationPropertyStoreService getConfigurationPropertyService(String namespace) throws ConfigurationPropertyStoreException {
                assertThat(namespace).isEqualTo("dss");
                return mockPropsStoreService ;
            }
        };

        // Inject our mock framework into the servlet.
        LogCapturingAccessCps servlet = new LogCapturingAccessCps();
        servlet.framework = mockFramework ;

        MockHttpRequest request = new MockHttpRequest("/dss/property/my.prop");
        request.setBody(
                "{\n"+
                        "  \"name\":\"my.prop\",\n"+
                        "  \"value\":\"myvalue\"\n"+
                        "}\n");
        MockHttpResponse response = new MockHttpResponse();

        // When...
        servlet.doPut(request,response);

        // Then...
        assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(response.getStatus()).isEqualTo(404); // resource not found.

        // Check that the error was good.
        String errorMessage = response.getPayloadAsErrorMessage();
        assertThat(errorMessage)
                .doesNotContain("Internal server error.")
                .contains("dss")
                .contains("is not found.")
        ;
    }



    @Test
    public void getPropertiesFromSecureGetRedacted() throws Exception {

        // Given...

        // A property store which can serve-up some test properties 'a' and 'c'
        IConfigurationPropertyStoreService mockPropsStoreService = new MockConfigurationPropertyStoreService() {
            @Override
            public Map<String, String> getAllProperties() {
                return new HashMap<>() {{ put("a","aValue"); put("c","cValue"); }};
            }
        };

        // A mock framework which passes back the property store.
        IFramework mockFramework = new MockFramework() {
            @Override
            public IConfigurationPropertyStoreService getConfigurationPropertyService(String namespace) throws ConfigurationPropertyStoreException {
                assertThat(namespace).isEqualTo("secure"); // Secure is write-enabled, but never read.
                return mockPropsStoreService ;
            }
        };

        // Inject our mock framework into the servlet.
        LogCapturingAccessCps servlet = new LogCapturingAccessCps();
        servlet.framework = mockFramework ;

        HttpServletRequest request = new MockHttpRequest("/secure");

        MockHttpResponse response = new MockHttpResponse();

        // When...
        servlet.doGet(request,response);

        // Then...
        assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(response.getStatus()).isEqualTo(200); // OK

        // The result should be an array of namespaces
        JsonReader jReader = response.getPayloadAsJsonReader();
        NameValuePair[] propertiesArray = gson.fromJson(jReader , NameValuePair[].class);

        // Transfer values into a java map.
        Map<String,String> properties = new HashMap<>();
        for ( NameValuePair pair : propertiesArray ) {
            properties.put(pair.name, pair.value);
        }

        assertThat(properties)
                .containsKey("a")
                .containsKey("c");
        assertThat(properties.get("a")).isEqualTo("********"); // The redacted value.
        assertThat(properties.get("c")).isEqualTo("********"); // The redacted value.
    }

    @Test
    public void testCanGetPropertiesUsingPrefixAndSuffixOk() throws Exception {

        // Given...
        String prefix = "abc";
        String suffix = "xyz";

        // A property store which can serve-up some test properties 'a' and 'c'
        IConfigurationPropertyStoreService mockPropsStoreService = new MockConfigurationPropertyStoreService() {
            @Override
            public Map<String, String> getAllProperties() {
                return new HashMap<>() {{
                    put("oknamespace.abc.a.xyz","abcValue");
                    put("oknamespace.abc.b.xyz","cValue");
                }};
            }
            @Override
            public String getProperty(String prefix, String suffix, String... infixes
                ) throws ConfigurationPropertyStoreException {
                return "abcValue";
            }
        };

        // A mock framework which passes back the property store.
        IFramework mockFramework = new MockFramework() {
            @Override
            public IConfigurationPropertyStoreService getConfigurationPropertyService(String namespace) throws ConfigurationPropertyStoreException {
                return mockPropsStoreService ;
            }
        };

        // Inject our mock framework into the servlet.
        LogCapturingAccessCps servlet = new LogCapturingAccessCps();
        servlet.framework = mockFramework ;

        String path = MessageFormat.format( "/oknamespace/prefix/{0}/suffix/{1}",prefix,suffix);
        String query = "infix=a";
        HttpServletRequest request = new MockHttpRequest(path,query);

        MockHttpResponse response = new MockHttpResponse();

        // When...
        servlet.doGet(request,response);

        // Then...
        assertThat(response.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(response.getStatus()).isEqualTo(200); // OK

        // The result should be a single object with a name, value pair
        JsonReader jReader = response.getPayloadAsJsonReader();
        NameValuePair property = gson.fromJson(jReader , NameValuePair.class);
        assertThat(property).isNotNull();
        assertThat(property.name).isNotNull().isEqualTo("oknamespace.abc.a.xyz");
        assertThat(property.value).isNotNull().isEqualTo("abcValue");
    }

}