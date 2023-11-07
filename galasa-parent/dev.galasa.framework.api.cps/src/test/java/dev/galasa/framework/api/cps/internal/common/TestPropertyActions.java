/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.common;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

import static org.assertj.core.api.Assertions.*;


public class TestPropertyActions{

    private IFramework getFramework(String namespace){
        
        IConfigurationPropertyStoreService cpsstore = new MockIConfigurationPropertyStoreService(namespace);
        IFramework framework = new MockFramework(cpsstore);

        return framework;
    }

    private String generateGalasaPropertyJson(String namespace, String propertyName, String propertyValue){
        return "{\n  \"apiVersion\": \"v1alpha1\",\n"+
        "  \"kind\": \"GalasaProperty\",\n"+
        "  \"metadata\": {\n"+
        "    \"namespace\": \""+namespace+"\",\n"+
        "    \"name\": \""+propertyName+"\"\n"+
        "  },\n"+
        "  \"data\": {\n"+
        "    \"value\": \""+propertyValue+"\"\n  }\n}";
    }

    @Test
    public void TestPropertyActionsIsPropertyValidCorrectPropertyStructureReturnsTrue() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        GalasaProperty property = new GalasaProperty("framework.property.name1","myvalue");
        
        //When...
        boolean isValid = propertyActions.isPropertyValid(property);

        //Then...
        assertThat(isValid).isTrue();
    }

    @Test
    public void TestPropertyActionsIsPropertyValidMissingNameReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        GalasaProperty property = new GalasaProperty("framework","","propertyvalue");
        
        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyActions.isPropertyValid(property);
        });
        
        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("name")).isTrue();
    }

    @Test
    public void TestPropertyActionsIsPropertyValidNullNameReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        GalasaProperty property = new GalasaProperty("framework",null,"propertyvalue");
        
        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyActions.isPropertyValid(property);
        });
        
        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("name")).isTrue();
    }

    @Test
    public void TestPropertyActionsIsPropertyValidBlankNameReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        GalasaProperty property = new GalasaProperty("framework","  ","propertyvalue");
        
        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyActions.isPropertyValid(property);
        });
        
        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("name")).isTrue();
    }

    @Test
    public void TestPropertyActionsIsPropertyValidMissingNamespaceReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        GalasaProperty property = new GalasaProperty("","property","propertyvalue");
        
        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyActions.isPropertyValid(property);
        });
        
        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("namespace")).isTrue();
    }

    @Test
    public void TestPropertyActionsIsPropertyValidNullNamespaceReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        GalasaProperty property = new GalasaProperty(null,"property","propertyvalue");
        
        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyActions.isPropertyValid(property);
        });
        
        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("namespace")).isTrue();
    }

    @Test
    public void TestPropertyActionsIsPropertyValidBlankNamespaceReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        GalasaProperty property = new GalasaProperty("   ","property","propertyvalue");
        
        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyActions.isPropertyValid(property);
        });
        
        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("namespace")).isTrue();
    }

    @Test
    public void TestPropertyActionsIsPropertyValidMissingValueReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        GalasaProperty property = new GalasaProperty("framework","proprtyname","");
        
        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyActions.isPropertyValid(property);
        });
        
        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("value")).isTrue();
    }

    @Test
    public void TestPropertyActionsIsPropertyValidNullValueReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        GalasaProperty property = new GalasaProperty("framework","name",null);
        
        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyActions.isPropertyValid(property);
        });
        
        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("value")).isTrue();
    }

    @Test
    public void TestPropertyActionsIsPropertyValidBlankValueReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        GalasaProperty property = new GalasaProperty("framework","property","   ");
        
        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyActions.isPropertyValid(property);
        });
        
        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("value")).isTrue();
    }

    @Test
    public void TestPropertyActionsIsPropertyValidMissingApiVersionReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        GalasaProperty property = new GalasaProperty("framework","name","propertyvalue", "");
        
        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyActions.isPropertyValid(property);
        });
        
        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("apiVersion")).isTrue();
    }

    @Test
    public void TestPropertyActionsIsPropertyValidNullApiVersionReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        GalasaProperty property = new GalasaProperty("framework", "property","propertyvalue", null);
        
        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyActions.isPropertyValid(property);
        });
        
        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("apiVersion")).isTrue();
    }

    @Test
    public void TestPropertyActionsIsPropertyValidBlankApiVersionReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        GalasaProperty property = new GalasaProperty("framework","property","propertyvalue", "   ");
        
        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyActions.isPropertyValid(property);
        });
        
        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("apiVersion")).isTrue();
    }

    @Test
    public void TestPropertyActionsisHiddenNamespaceNotHiddenNamespaceReturnsFalse(){
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        
        //When...
        boolean isHidden = propertyActions.isHiddenNamespace("mynamespace");
        
        //Then...
        assertThat(isHidden).isFalse();
    }

    @Test
    public void TestPropertyActionsisHiddenNamespaceWithHiddenNamespaceReturnsTrue(){
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        
        //When...
        boolean isHidden = propertyActions.isHiddenNamespace("dss");
        
        //Then...
        assertThat(isHidden).isTrue();
    }

    @Test
    public void TestPropertyActionsIsSecureNamespaceWithNormalNamespaceReturnsFalse(){
        //Given...
        String namespace = "mynamespace";
        
        //When...
        boolean isSecure = PropertyActions.isSecureNamespace(namespace);
        
        //Then...
        assertThat(isSecure).isFalse();
    }

    @Test
    public void TestPropertyActionsIsSecureNamespaceWithSecureNamespaceReturnsTrue(){
        //Given...
        String namespace = "secure";
        
        //When...
        boolean isSecure = PropertyActions.isSecureNamespace(namespace);
        
        //Then...
        assertThat(isSecure).isTrue();
    }

    @Test
    public void TestPropertyActionsGetNameSpaceTypeNormalNamespaceReturnsNormal(){
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        
        //When...
        String namespaceType = propertyActions.getNamespaceType("normal");
        
        //Then...
        assertThat(namespaceType).isEqualTo("normal");
    }

     @Test
    public void TestPropertyActionsGetNameSpaceTypeSecureNamespaceReturnsSecure(){
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("secure"));

        //When...
        String namespaceType = propertyActions.getNamespaceType("secure");
        
        //Then...
        assertThat(namespaceType).isEqualTo("secure");
    }

    @Test
    public void TestPropertyActionsGetAllPropertiesReturnsEmpty() throws ConfigurationPropertyStoreException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("empty"));

        //When...
        Map<String, String> properties = propertyActions.getAllProperties("empty");

        //Then...
        assertThat(properties).isEmpty();
    }

    @Test
    public void TestPropertyActionsGetAllPropertiesReturnsConfigurationStoreException(){
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        
        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyActions.getAllProperties("error");
        });
        
        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getClass()).isEqualTo(ConfigurationPropertyStoreException.class);
    }

    @Test
    public void TestPropertyActionsGetAllPropertiesReturnsProperties() throws ConfigurationPropertyStoreException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        
        //When...
        Map<String, String> properties = propertyActions.getAllProperties("framework");

        //Then...
        assertThat(properties).isNotEmpty();
        assertThat(properties.containsKey("framework.property1")).isTrue();
    }

    @Test
    public void TestPropertyActionsGetProtectedValueNormalNamespaceReturnsValue(){
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));

        //When...
        String value = propertyActions.getProtectedValue("mypropertyvalue", "framework");

        //Then...
        assertThat(value).isEqualTo("mypropertyvalue");
    }

    @Test
    public void TestPropertyActionsGetProtectedValueSecureNamespaceReturnsRedactedValue(){
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));

        //When...
        String value = propertyActions.getProtectedValue("secureValue", "secure");
        
        //Then...
        assertThat(value).isEqualTo("********");
    }

    @Test
    public void TestPropertyActionsCheckPropertyExistsExistingPropertyReturnsTrue() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));

        //When...
        boolean exists = propertyActions.checkPropertyExists("framework","property1");

        //Then...
        assertThat(exists).isTrue();
    }

    @Test
    public void TestPropertyActionsCheckPropertyExistsNewropertyReturnsFalse() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));

        //When...
        boolean exists = propertyActions.checkPropertyExists("framework","notaproperty");

        //Then...
        assertThat(exists).isFalse();
    }

    @Test
    public void TestPropertyActionsCheckGalasaPropertyExistsExistingPropertyReturnsTrue() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        GalasaProperty property = new GalasaProperty("framework.property1", "value1");

        //When...
        boolean exists = propertyActions.checkGalasaPropertyExists(property);

        //Then...
        assertThat(exists).isTrue();
    }

    @Test
    public void TestPropertyActionsCheckGalasaPropertyExistsNewPropertyReturnsFalse() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        GalasaProperty property = new GalasaProperty("framework.property10", "value1");

        //When...
        boolean exists = propertyActions.checkGalasaPropertyExists(property);

        //Then...
        assertThat(exists).isFalse();
    }

    @Test
    public void TestPropertyActionsRetrieveSinglePropertyExistingPropertyReturnsProperty() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        
        //When...
        Map.Entry<String,String> property = propertyActions.retrieveSingleProperty("framework", "property1");

        //Then...
        assertThat(property.getKey()).isEqualTo("framework.property1");
        assertThat(property.getValue()).isEqualTo("value1");
    }

    @Test
    public void TestPropertyActionsRetrieveSinglePropertyExistingSecurePropertyReturnsRedactedProperty() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("secure"));
        
        //When...
        Map.Entry<String, String> property = propertyActions.retrieveSingleProperty("secure", "property1");
        
        //Then...
        assertThat(property.getKey()).isEqualTo("secure.property1");
        assertThat(property.getValue()).isEqualTo("********");
}

    @Test
    public void TestPropertyActionsRetrieveSinglePropertyBadNamespaceReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        
        //When...
        Throwable thrown = catchThrowable(() -> {
           propertyActions.retrieveSingleProperty("error", "property1");
        });
        
        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("Error occured when trying to access namespace")).isTrue();
    }

    @Test
    public void TestPropertyActionsRetrieveSinglePropertyNullNamespaceReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        
        //When...
        Throwable thrown = catchThrowable(() -> {
           propertyActions.retrieveSingleProperty(null, "property1");
        });
        
        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("Error occured when trying to access namespace")).isTrue();
    }

    @Test
    public void TestPropertyActionsRetrieveSinglePropertyNullNameReturnsNull() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        
        //When...
        Map.Entry<String,String> property = propertyActions.retrieveSingleProperty("framework", null);
        
        //Then...
        assertThat(property).isNull();
    }

    @Test
    public void TestPropertyActionsRetrievesSingleBadPropertyReturnsNull() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        
        //When...
        Map.Entry<String, String> property = propertyActions.retrieveSingleProperty("framework", "custardoughnuts");

        //Then...
        assertThat(property).isNull();
    }

    @Test
    public void TestPropertyActionsSetPropertyCreateNewPropertyReturnsSuccess() throws InternalServletException, FrameworkException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        String propertyName = "framework.jindex";
        String propertyValue = "jindexvalue";
        GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
        boolean updateProperty = false;
        
        //When...
        propertyActions.setProperty(property, updateProperty);

        //Then...   
        Map.Entry<String, String> retrievedProperty = propertyActions.retrieveSingleProperty("framework", "jindex");
        assertThat(retrievedProperty.getKey()).isEqualTo(propertyName);
        assertThat(retrievedProperty.getValue()).isEqualTo(propertyValue);
    }

    @Test
    public void TestPropertyActionsSetPropertyCreateExistingPropertyReturnsError(){
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        String propertyName = "framework.property1";
        String propertyValue = "value1";
        GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
        boolean updateProperty = false;
        
        //When...
        Throwable thrown = catchThrowable(() -> {
           propertyActions.setProperty(property, updateProperty);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("The property name provided already exists")).isTrue();
    }

    @Test
    public void TestPropertyActionsSetPropertyUpdateExistingPropertyReturnsSuccess() throws InternalServletException, FrameworkException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        String propertyName = "framework.property1";
        String propertyValue = "jindexvalue";
        GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
        boolean updateProperty = true;
        
        
        //When...
        propertyActions.setProperty(property, updateProperty);

        //Then...   
        Map.Entry<String, String> retrievedProperty = propertyActions.retrieveSingleProperty("framework", "property1");
        assertThat(retrievedProperty.getKey()).isEqualTo(propertyName);
        assertThat(retrievedProperty.getValue()).isEqualTo(propertyValue);
    }

    @Test
    public void TestPropertyActionsSetPropertyUpdateNewPropertyReturnsError(){
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        String propertyName = "framework.random";
        String propertyValue = "value1";
        GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
        boolean updateProperty = true;
        
        //When...
        Throwable thrown = catchThrowable(() -> {
           propertyActions.setProperty(property, updateProperty);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("The property name provided is invalid")).isTrue();
    }

@Test
    public void TestPropertyActionsSetGalasaPropertyCreateNewPropertyReturnsSuccess() throws InternalServletException, FrameworkException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        String propertyName = "framework.jindex";
        String propertyValue = "jindexvalue";
        GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
        String action = "create";
        
        //When...
        propertyActions.setGalasaProperty(property, action);

        //Then...   
        Map.Entry<String, String> retrievedProperty = propertyActions.retrieveSingleProperty("framework", "jindex");
        assertThat(retrievedProperty.getKey()).isEqualTo(propertyName);
        assertThat(retrievedProperty.getValue()).isEqualTo(propertyValue);
    }

    @Test
    public void TestPropertyActionsSetGalasaPropertyCreateExistingPropertyReturnsError(){
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        String propertyName = "framework.property1";
        String propertyValue = "value1";
        GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
        String action = "create";
        
        //When...
        Throwable thrown = catchThrowable(() -> {
           propertyActions.setGalasaProperty(property, action);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("The property name provided already exists")).isTrue();
    }

    @Test
    public void TestPropertyActionsSetGalasaPropertyUpdateExistingPropertyReturnsSuccess() throws InternalServletException, FrameworkException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        String propertyName = "framework.property1";
        String propertyValue = "jindexvalue";
        GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
        String action = "update";
        
        
        //When...
        propertyActions.setGalasaProperty(property, action);

        //Then...   
        Map.Entry<String, String> retrievedProperty = propertyActions.retrieveSingleProperty("framework", "property1");
        assertThat(retrievedProperty.getKey()).isEqualTo(propertyName);
        assertThat(retrievedProperty.getValue()).isEqualTo(propertyValue);
    }

    @Test
    public void TestPropertyActionsSetGalasaPropertyUpdateNewPropertyReturnsError(){
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        String propertyName = "framework.random";
        String propertyValue = "value1";
        GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
        String action = "update";
        
        //When...
        Throwable thrown = catchThrowable(() -> {
           propertyActions.setGalasaProperty(property, action);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("The property name provided is invalid")).isTrue();
    }

    @Test
    public void TestPropertyActionsSetGalasaPropertyApplyExistingPropertyReturnsSuccess() throws InternalServletException, FrameworkException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        String propertyName = "framework.property1";
        String propertyValue = "jindexvalue";
        GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
        String action = "apply";
        
        
        //When...
        propertyActions.setGalasaProperty(property, action);

        //Then...   
        Map.Entry<String, String> retrievedProperty = propertyActions.retrieveSingleProperty("framework", "property1");
        assertThat(retrievedProperty.getKey()).isEqualTo(propertyName);
        assertThat(retrievedProperty.getValue()).isEqualTo(propertyValue);
    }

    @Test
    public void TestPropertyActionsSetGalasaPropertyApplyNewPropertyReturnsSuccess() throws InternalServletException, FrameworkException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        String propertyName = "framework.randomproperty";
        String propertyValue = "jindexvalue";
        GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
        String action = "apply";
        
        
        //When...
        propertyActions.setGalasaProperty(property, action);

        //Then...   
        Map.Entry<String, String> retrievedProperty = propertyActions.retrieveSingleProperty("framework", "randomproperty");
        assertThat(retrievedProperty.getKey()).isEqualTo(propertyName);
        assertThat(retrievedProperty.getValue()).isEqualTo(propertyValue);
    }

    @Test
    public void TestPropertyActionsGetGalasaPropertyFromJsonStringReturnsProperty() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        String propertyJson = generateGalasaPropertyJson("framework","newproperty","newvalue");

        //When...
        GalasaProperty property = propertyActions.getGalasaPropertyfromJsonString(propertyJson);

        //Then...
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestPropertyActionsGetGalasaPropertyFromJsonStringBadStringReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        String propertyJson = generateGalasaPropertyJson("","","");

        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyActions.getGalasaPropertyfromJsonString(propertyJson);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("to GalasaProperty.")).isTrue();
    }

    @Test
    public void TestPropertyActionsGetPropertyFromRequestBodyReturnsProperty() throws InternalServletException, IOException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        String propertyJson = generateGalasaPropertyJson("framework","newproperty","newvalue");
        HttpServletRequest request =  new MockHttpServletRequest("", propertyJson, "POST");

        //When...
        GalasaProperty property = propertyActions.getPropertyFromRequestBody(request);

        //Then...
        assertThat(property.isPropertyValid()).isTrue();
    }

    @Test
    public void TestPropertyActionsGetPropertyFromRequestBodyBadBodyReturnsError() throws InternalServletException{
        //Given...
        PropertyActions propertyActions = new PropertyActions(getFramework("framework"));
        String propertyJson = generateGalasaPropertyJson("","","");
        HttpServletRequest request =  new MockHttpServletRequest("", propertyJson, "POST");

        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyActions.getPropertyFromRequestBody(request);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage().contains("to GalasaProperty.")).isTrue();
    }
}

    