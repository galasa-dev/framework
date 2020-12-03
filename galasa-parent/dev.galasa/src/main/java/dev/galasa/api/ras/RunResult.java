package dev.galasa.api.ras;

import java.util.List;

public class RunResult {
   
   private String runId;
   private List<Artifact> artifacts;
   private TestStructure testStructure;
   
   public RunResult(String runId, List<Artifact> artifacts, TestStructure testStructure) {
      this.runId = runId;
      this.artifacts = artifacts;
      this.testStructure = testStructure;
   }

   public String getRunId() {
      return runId;
   }

   public void setRunId(String runId) {
      this.runId = runId;
   }

   public List<Artifact> getArtifacts() {
      return artifacts;
   }

   public void setArtifacts(List<Artifact> artifacts) {
      this.artifacts = artifacts;
   }

   public TestStructure getTestStructure() {
      return testStructure;
   }

   public void setTestStructure(TestStructure testStructure) {
      this.testStructure = testStructure;
   }
   
   
}
