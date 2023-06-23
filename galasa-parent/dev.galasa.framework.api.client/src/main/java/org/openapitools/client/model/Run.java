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


package org.openapitools.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openapitools.client.model.Artifact;
import org.openapitools.client.model.TestStructure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openapitools.client.JSON;

/**
 * Run
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-06-23T09:56:25.063401+01:00[Europe/London]")
public class Run {
  public static final String SERIALIZED_NAME_RUN_ID = "runId";
  @SerializedName(SERIALIZED_NAME_RUN_ID)
  private String runId;

  public static final String SERIALIZED_NAME_TEST_STRUCTURE = "testStructure";
  @SerializedName(SERIALIZED_NAME_TEST_STRUCTURE)
  private TestStructure testStructure;

  public static final String SERIALIZED_NAME_ARTIFACTS = "artifacts";
  @SerializedName(SERIALIZED_NAME_ARTIFACTS)
  private List<Artifact> artifacts = null;

  public Run() {
  }

  public Run runId(String runId) {
    
    this.runId = runId;
    return this;
  }

   /**
   * Get runId
   * @return runId
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public String getRunId() {
    return runId;
  }


  public void setRunId(String runId) {
    this.runId = runId;
  }


  public Run testStructure(TestStructure testStructure) {
    
    this.testStructure = testStructure;
    return this;
  }

   /**
   * Get testStructure
   * @return testStructure
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public TestStructure getTestStructure() {
    return testStructure;
  }


  public void setTestStructure(TestStructure testStructure) {
    this.testStructure = testStructure;
  }


  public Run artifacts(List<Artifact> artifacts) {
    
    this.artifacts = artifacts;
    return this;
  }

  public Run addArtifactsItem(Artifact artifactsItem) {
    if (this.artifacts == null) {
      this.artifacts = new ArrayList<>();
    }
    this.artifacts.add(artifactsItem);
    return this;
  }

   /**
   * Get artifacts
   * @return artifacts
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public List<Artifact> getArtifacts() {
    return artifacts;
  }


  public void setArtifacts(List<Artifact> artifacts) {
    this.artifacts = artifacts;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Run run = (Run) o;
    return Objects.equals(this.runId, run.runId) &&
        Objects.equals(this.testStructure, run.testStructure) &&
        Objects.equals(this.artifacts, run.artifacts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(runId, testStructure, artifacts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Run {\n");
    sb.append("    runId: ").append(toIndentedString(runId)).append("\n");
    sb.append("    testStructure: ").append(toIndentedString(testStructure)).append("\n");
    sb.append("    artifacts: ").append(toIndentedString(artifacts)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


  public static HashSet<String> openapiFields;
  public static HashSet<String> openapiRequiredFields;

  static {
    // a set of all properties/fields (JSON key names)
    openapiFields = new HashSet<String>();
    openapiFields.add("runId");
    openapiFields.add("testStructure");
    openapiFields.add("artifacts");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
  }

 /**
  * Validates the JSON Object and throws an exception if issues found
  *
  * @param jsonObj JSON Object
  * @throws IOException if the JSON Object is invalid with respect to Run
  */
  public static void validateJsonObject(JsonObject jsonObj) throws IOException {
      if (jsonObj == null) {
        if (Run.openapiRequiredFields.isEmpty()) {
          return;
        } else { // has required fields
          throw new IllegalArgumentException(String.format("The required field(s) %s in Run is not found in the empty JSON string", Run.openapiRequiredFields.toString()));
        }
      }

      Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
      // check to see if the JSON string contains additional fields
      for (Entry<String, JsonElement> entry : entries) {
        if (!Run.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `Run` properties. JSON: %s", entry.getKey(), jsonObj.toString()));
        }
      }
      if ((jsonObj.get("runId") != null && !jsonObj.get("runId").isJsonNull()) && !jsonObj.get("runId").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `runId` to be a primitive type in the JSON string but got `%s`", jsonObj.get("runId").toString()));
      }
      // validate the optional field `testStructure`
      if (jsonObj.get("testStructure") != null && !jsonObj.get("testStructure").isJsonNull()) {
        TestStructure.validateJsonObject(jsonObj.getAsJsonObject("testStructure"));
      }
      if (jsonObj.get("artifacts") != null && !jsonObj.get("artifacts").isJsonNull()) {
        JsonArray jsonArrayartifacts = jsonObj.getAsJsonArray("artifacts");
        if (jsonArrayartifacts != null) {
          // ensure the json data is an array
          if (!jsonObj.get("artifacts").isJsonArray()) {
            throw new IllegalArgumentException(String.format("Expected the field `artifacts` to be an array in the JSON string but got `%s`", jsonObj.get("artifacts").toString()));
          }

          // validate the optional field `artifacts` (array)
          for (int i = 0; i < jsonArrayartifacts.size(); i++) {
            Artifact.validateJsonObject(jsonArrayartifacts.get(i).getAsJsonObject());
          };
        }
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!Run.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'Run' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<Run> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(Run.class));

       return (TypeAdapter<T>) new TypeAdapter<Run>() {
           @Override
           public void write(JsonWriter out, Run value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public Run read(JsonReader in) throws IOException {
             JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
             validateJsonObject(jsonObj);
             return thisAdapter.fromJsonTree(jsonObj);
           }

       }.nullSafe();
    }
  }

 /**
  * Create an instance of Run given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of Run
  * @throws IOException if the JSON string is invalid with respect to Run
  */
  public static Run fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, Run.class);
  }

 /**
  * Convert an instance of Run to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

