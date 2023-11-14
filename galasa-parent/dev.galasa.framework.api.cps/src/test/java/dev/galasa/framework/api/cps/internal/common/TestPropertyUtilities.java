// /*
//  * Copyright contributors to the Galasa project
//  *
//  * SPDX-License-Identifier: EPL-2.0
//  */
// package dev.galasa.framework.api.cps.internal.common;

// import java.io.IOException;
// import java.util.*;

// import javax.servlet.http.HttpServletRequest;

// import org.junit.Test;

// import dev.galasa.framework.api.common.InternalServletException;
// import dev.galasa.framework.api.common.mocks.MockFramework;
// import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
// import dev.galasa.framework.api.common.mocks.MockIConfigurationPropertyStoreService;
// import dev.galasa.framework.api.common.resources.GalasaProperty;
// import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
// import dev.galasa.framework.spi.FrameworkException;
// import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
// import dev.galasa.framework.spi.IFramework;

// import static org.assertj.core.api.Assertions.*;


// public class TestPropertyUtilities{

//     private IFramework getFramework(String namespace){
        
//         IConfigurationPropertyStoreService cpsstore = new MockIConfigurationPropertyStoreService(namespace);
//         IFramework framework = new MockFramework(cpsstore);

//         return framework;
//     }

//     private String generateGalasaPropertyJson(String namespace, String propertyName, String propertyValue){
//         return "{\n  \"apiVersion\": \"galasa-dev/v1alpha1\",\n"+
//         "  \"kind\": \"GalasaProperty\",\n"+
//         "  \"metadata\": {\n"+
//         "    \"namespace\": \""+namespace+"\",\n"+
//         "    \"name\": \""+propertyName+"\"\n"+
//         "  },\n"+
//         "  \"data\": {\n"+
//         "    \"value\": \""+propertyValue+"\"\n  }\n}";
//     }

//     @Test
//     public void TestpropertyUtilityIsPropertyValidCorrectPropertyStructureReturnsTrue() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         GalasaProperty property = new GalasaProperty("framework.property.name1","myvalue");
        
//         //When...
//         boolean isValid = propertyUtility.isPropertyValid(property);

//         //Then...
//         assertThat(isValid).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityIsPropertyValidMissingNameReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         GalasaProperty property = new GalasaProperty("framework","","propertyvalue");
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//             propertyUtility.isPropertyValid(property);
//         });
        
//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("name")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityIsPropertyValidNullNameReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         GalasaProperty property = new GalasaProperty("framework",null,"propertyvalue");
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//             propertyUtility.isPropertyValid(property);
//         });
        
//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("name")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityIsPropertyValidBlankNameReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         GalasaProperty property = new GalasaProperty("framework","  ","propertyvalue");
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//             propertyUtility.isPropertyValid(property);
//         });
        
//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("name")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityIsPropertyValidMissingNamespaceReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         GalasaProperty property = new GalasaProperty("","property","propertyvalue");
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//             propertyUtility.isPropertyValid(property);
//         });
        
//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("namespace")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityIsPropertyValidNullNamespaceReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         GalasaProperty property = new GalasaProperty(null,"property","propertyvalue");
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//             propertyUtility.isPropertyValid(property);
//         });
        
//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("namespace")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityIsPropertyValidBlankNamespaceReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         GalasaProperty property = new GalasaProperty("   ","property","propertyvalue");
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//             propertyUtility.isPropertyValid(property);
//         });
        
//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("namespace")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityIsPropertyValidMissingValueReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         GalasaProperty property = new GalasaProperty("framework","proprtyname","");
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//             propertyUtility.isPropertyValid(property);
//         });
        
//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("value")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityIsPropertyValidNullValueReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         GalasaProperty property = new GalasaProperty("framework","name",null);
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//             propertyUtility.isPropertyValid(property);
//         });
        
//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("value")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityIsPropertyValidBlankValueReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         GalasaProperty property = new GalasaProperty("framework","property","   ");
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//             propertyUtility.isPropertyValid(property);
//         });
        
//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("value")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityIsPropertyValidMissingApiVersionReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         GalasaProperty property = new GalasaProperty("framework","name","propertyvalue", "");
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//             propertyUtility.isPropertyValid(property);
//         });
        
//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("apiVersion")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityIsPropertyValidNullApiVersionReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         GalasaProperty property = new GalasaProperty("framework", "property","propertyvalue", null);
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//             propertyUtility.isPropertyValid(property);
//         });
        
//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("apiVersion")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityIsPropertyValidBlankApiVersionReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         GalasaProperty property = new GalasaProperty("framework","property","propertyvalue", "   ");
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//             propertyUtility.isPropertyValid(property);
//         });
        
//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("apiVersion")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityisHiddenNamespaceNotHiddenNamespaceReturnsFalse(){
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
        
//         //When...
//         boolean isHidden = propertyUtility.isHiddenNamespace("mynamespace");
        
//         //Then...
//         assertThat(isHidden).isFalse();
//     }

//     @Test
//     public void TestpropertyUtilityisHiddenNamespaceWithHiddenNamespaceReturnsTrue(){
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
        
//         //When...
//         boolean isHidden = propertyUtility.isHiddenNamespace("dss");
        
//         //Then...
//         assertThat(isHidden).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityIsSecureNamespaceWithNormalNamespaceReturnsFalse(){
//         //Given...
//         String namespace = "mynamespace";
        
//         //When...
//         boolean isSecure = PropertyUtilities.isSecureNamespace(namespace);
        
//         //Then...
//         assertThat(isSecure).isFalse();
//     }

//     @Test
//     public void TestpropertyUtilityIsSecureNamespaceWithSecureNamespaceReturnsTrue(){
//         //Given...
//         String namespace = "secure";
        
//         //When...
//         boolean isSecure = PropertyUtilities.isSecureNamespace(namespace);
        
//         //Then...
//         assertThat(isSecure).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityGetNameSpaceTypeNormalNamespaceReturnsNormal(){
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
        
//         //When...
//         String namespaceType = propertyUtility.getNamespaceType("normal");
        
//         //Then...
//         assertThat(namespaceType).isEqualTo("normal");
//     }

//      @Test
//     public void TestpropertyUtilityGetNameSpaceTypeSecureNamespaceReturnsSecure(){
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("secure"));

//         //When...
//         String namespaceType = propertyUtility.getNamespaceType("secure");
        
//         //Then...
//         assertThat(namespaceType).isEqualTo("secure");
//     }

//     @Test
//     public void TestpropertyUtilityGetAllPropertiesReturnsEmpty() throws ConfigurationPropertyStoreException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("empty"));

//         //When...
//         Map<String, String> properties = propertyUtility.getAllProperties("empty");

//         //Then...
//         assertThat(properties).isEmpty();
//     }

//     @Test
//     public void TestpropertyUtilityGetAllPropertiesReturnsConfigurationStoreException(){
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//             propertyUtility.getAllProperties("error");
//         });
        
//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getClass()).isEqualTo(ConfigurationPropertyStoreException.class);
//     }

//     @Test
//     public void TestpropertyUtilityGetAllPropertiesReturnsProperties() throws ConfigurationPropertyStoreException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
        
//         //When...
//         Map<String, String> properties = propertyUtility.getAllProperties("framework");

//         //Then...
//         assertThat(properties).isNotEmpty();
//         assertThat(properties.containsKey("framework.property1")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityGetProtectedValueNormalNamespaceReturnsValue(){
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));

//         //When...
//         String value = propertyUtility.getProtectedValue("mypropertyvalue", "framework");

//         //Then...
//         assertThat(value).isEqualTo("mypropertyvalue");
//     }

//     @Test
//     public void TestpropertyUtilityGetProtectedValueSecureNamespaceReturnsRedactedValue(){
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));

//         //When...
//         String value = propertyUtility.getProtectedValue("secureValue", "secure");
        
//         //Then...
//         assertThat(value).isEqualTo("********");
//     }

//     @Test
//     public void TestpropertyUtilityCheckPropertyExistsExistingPropertyReturnsTrue() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));

//         //When...
//         boolean exists = propertyUtility.checkPropertyExists("framework","property1");

//         //Then...
//         assertThat(exists).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityCheckPropertyExistsNewropertyReturnsFalse() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));

//         //When...
//         boolean exists = propertyUtility.checkPropertyExists("framework","notaproperty");

//         //Then...
//         assertThat(exists).isFalse();
//     }

//     @Test
//     public void TestpropertyUtilityCheckGalasaPropertyExistsExistingPropertyReturnsTrue() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         GalasaProperty property = new GalasaProperty("framework.property1", "value1");

//         //When...
//         boolean exists = propertyUtility.checkGalasaPropertyExists(property);

//         //Then...
//         assertThat(exists).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityCheckGalasaPropertyExistsNewPropertyReturnsFalse() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         GalasaProperty property = new GalasaProperty("framework.property10", "value1");

//         //When...
//         boolean exists = propertyUtility.checkGalasaPropertyExists(property);

//         //Then...
//         assertThat(exists).isFalse();
//     }

//     @Test
//     public void TestpropertyUtilityRetrieveSinglePropertyExistingPropertyReturnsProperty() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
        
//         //When...
//         Map.Entry<String,String> property = propertyUtility.retrieveSingleProperty("framework", "property1");

//         //Then...
//         assertThat(property.getKey()).isEqualTo("framework.property1");
//         assertThat(property.getValue()).isEqualTo("value1");
//     }

//     @Test
//     public void TestpropertyUtilityRetrieveSinglePropertyExistingSecurePropertyReturnsRedactedProperty() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("secure"));
        
//         //When...
//         Map.Entry<String, String> property = propertyUtility.retrieveSingleProperty("secure", "property1");
        
//         //Then...
//         assertThat(property.getKey()).isEqualTo("secure.property1");
//         assertThat(property.getValue()).isEqualTo("********");
// }

//     @Test
//     public void TestpropertyUtilityRetrieveSinglePropertyBadNamespaceReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//            propertyUtility.retrieveSingleProperty("error", "property1");
//         });
        
//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("Error occured when trying to access namespace")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityRetrieveSinglePropertyNullNamespaceReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//            propertyUtility.retrieveSingleProperty(null, "property1");
//         });
        
//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("Error occured when trying to access namespace")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityRetrieveSinglePropertyNullNameReturnsNull() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
        
//         //When...
//         Map.Entry<String,String> property = propertyUtility.retrieveSingleProperty("framework", null);
        
//         //Then...
//         assertThat(property).isNull();
//     }

//     @Test
//     public void TestpropertyUtilityRetrievesSingleBadPropertyReturnsNull() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
        
//         //When...
//         Map.Entry<String, String> property = propertyUtility.retrieveSingleProperty("framework", "custardoughnuts");

//         //Then...
//         assertThat(property).isNull();
//     }

//     @Test
//     public void TestpropertyUtilitySetPropertyCreateNewPropertyReturnsSuccess() throws InternalServletException, FrameworkException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         String propertyName = "framework.jindex";
//         String propertyValue = "jindexvalue";
//         GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
//         boolean updateProperty = false;
        
//         //When...
//         propertyUtility.setProperty(property, updateProperty);

//         //Then...   
//         Map.Entry<String, String> retrievedProperty = propertyUtility.retrieveSingleProperty("framework", "jindex");
//         assertThat(retrievedProperty.getKey()).isEqualTo(propertyName);
//         assertThat(retrievedProperty.getValue()).isEqualTo(propertyValue);
//     }

//     @Test
//     public void TestpropertyUtilitySetPropertyCreateExistingPropertyReturnsError(){
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         String propertyName = "framework.property1";
//         String propertyValue = "value1";
//         GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
//         boolean updateProperty = false;
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//            propertyUtility.setProperty(property, updateProperty);
//         });

//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("The property name provided already exists")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilitySetPropertyUpdateExistingPropertyReturnsSuccess() throws InternalServletException, FrameworkException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         String propertyName = "framework.property1";
//         String propertyValue = "jindexvalue";
//         GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
//         boolean updateProperty = true;
        
        
//         //When...
//         propertyUtility.setProperty(property, updateProperty);

//         //Then...   
//         Map.Entry<String, String> retrievedProperty = propertyUtility.retrieveSingleProperty("framework", "property1");
//         assertThat(retrievedProperty.getKey()).isEqualTo(propertyName);
//         assertThat(retrievedProperty.getValue()).isEqualTo(propertyValue);
//     }

//     @Test
//     public void TestpropertyUtilitySetPropertyUpdateNewPropertyReturnsError(){
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         String propertyName = "framework.random";
//         String propertyValue = "value1";
//         GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
//         boolean updateProperty = true;
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//            propertyUtility.setProperty(property, updateProperty);
//         });

//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("The property name provided is invalid")).isTrue();
//     }

// @Test
//     public void TestpropertyUtilitySetGalasaPropertyCreateNewPropertyReturnsSuccess() throws InternalServletException, FrameworkException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         String propertyName = "framework.jindex";
//         String propertyValue = "jindexvalue";
//         GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
//         String action = "create";
        
//         //When...
//         propertyUtility.setGalasaProperty(property, action);

//         //Then...   
//         Map.Entry<String, String> retrievedProperty = propertyUtility.retrieveSingleProperty("framework", "jindex");
//         assertThat(retrievedProperty.getKey()).isEqualTo(propertyName);
//         assertThat(retrievedProperty.getValue()).isEqualTo(propertyValue);
//     }

//     @Test
//     public void TestpropertyUtilitySetGalasaPropertyCreateExistingPropertyReturnsError(){
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         String propertyName = "framework.property1";
//         String propertyValue = "value1";
//         GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
//         String action = "create";
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//            propertyUtility.setGalasaProperty(property, action);
//         });

//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("The property name provided already exists")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilitySetGalasaPropertyUpdateExistingPropertyReturnsSuccess() throws InternalServletException, FrameworkException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         String propertyName = "framework.property1";
//         String propertyValue = "jindexvalue";
//         GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
//         String action = "update";
        
        
//         //When...
//         propertyUtility.setGalasaProperty(property, action);

//         //Then...   
//         Map.Entry<String, String> retrievedProperty = propertyUtility.retrieveSingleProperty("framework", "property1");
//         assertThat(retrievedProperty.getKey()).isEqualTo(propertyName);
//         assertThat(retrievedProperty.getValue()).isEqualTo(propertyValue);
//     }

//     @Test
//     public void TestpropertyUtilitySetGalasaPropertyUpdateNewPropertyReturnsError(){
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         String propertyName = "framework.random";
//         String propertyValue = "value1";
//         GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
//         String action = "update";
        
//         //When...
//         Throwable thrown = catchThrowable(() -> {
//            propertyUtility.setGalasaProperty(property, action);
//         });

//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("The property name provided is invalid")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilitySetGalasaPropertyApplyExistingPropertyReturnsSuccess() throws InternalServletException, FrameworkException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         String propertyName = "framework.property1";
//         String propertyValue = "jindexvalue";
//         GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
//         String action = "apply";
        
        
//         //When...
//         propertyUtility.setGalasaProperty(property, action);

//         //Then...   
//         Map.Entry<String, String> retrievedProperty = propertyUtility.retrieveSingleProperty("framework", "property1");
//         assertThat(retrievedProperty.getKey()).isEqualTo(propertyName);
//         assertThat(retrievedProperty.getValue()).isEqualTo(propertyValue);
//     }

//     @Test
//     public void TestpropertyUtilitySetGalasaPropertyApplyNewPropertyReturnsSuccess() throws InternalServletException, FrameworkException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         String propertyName = "framework.randomproperty";
//         String propertyValue = "jindexvalue";
//         GalasaProperty property = new GalasaProperty(propertyName, propertyValue);
//         String action = "apply";
        
        
//         //When...
//         propertyUtility.setGalasaProperty(property, action);

//         //Then...   
//         Map.Entry<String, String> retrievedProperty = propertyUtility.retrieveSingleProperty("framework", "randomproperty");
//         assertThat(retrievedProperty.getKey()).isEqualTo(propertyName);
//         assertThat(retrievedProperty.getValue()).isEqualTo(propertyValue);
//     }

//     @Test
//     public void TestpropertyUtilityGetGalasaPropertyFromJsonStringReturnsProperty() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         String propertyJson = generateGalasaPropertyJson("framework","newproperty","newvalue");

//         //When...
//         GalasaProperty property = propertyUtility.getGalasaPropertyfromJsonString(propertyJson);

//         //Then...
//         assertThat(property.isPropertyValid()).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityGetGalasaPropertyFromJsonStringBadStringReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         String propertyJson = generateGalasaPropertyJson("","","");

//         //When...
//         Throwable thrown = catchThrowable(() -> {
//             propertyUtility.getGalasaPropertyfromJsonString(propertyJson);
//         });

//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("to GalasaProperty.")).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityGetPropertyFromRequestBodyReturnsProperty() throws InternalServletException, IOException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         String propertyJson = generateGalasaPropertyJson("framework","newproperty","newvalue");
//         HttpServletRequest request =  new MockHttpServletRequest("", propertyJson, "POST");

//         //When...
//         GalasaProperty property = propertyUtility.getPropertyFromRequestBody(request);

//         //Then...
//         assertThat(property.isPropertyValid()).isTrue();
//     }

//     @Test
//     public void TestpropertyUtilityGetPropertyFromRequestBodyBadBodyReturnsError() throws InternalServletException{
//         //Given...
//         PropertyUtilities propertyUtility = new PropertyUtilities(getFramework("framework"));
//         String propertyJson = generateGalasaPropertyJson("","","");
//         HttpServletRequest request =  new MockHttpServletRequest("", propertyJson, "POST");

//         //When...
//         Throwable thrown = catchThrowable(() -> {
//             propertyUtility.getPropertyFromRequestBody(request);
//         });

//         //Then...
//         assertThat(thrown).isNotNull();
//         assertThat(thrown.getMessage().contains("to GalasaProperty.")).isTrue();
//     }
// }

    