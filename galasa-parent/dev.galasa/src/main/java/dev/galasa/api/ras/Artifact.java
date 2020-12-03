package dev.galasa.api.ras;

public class Artifact {
   
   private String artifactPath;
   private String contentType;
   
   public Artifact(String artifactPath, String contentType) {
      this.artifactPath = artifactPath;
      this.contentType = contentType;
   }

   public String getArtifactPath() {
      return artifactPath;
   }

   public void setArtifactPath(String artifactPath) {
      this.artifactPath = artifactPath;
   }

   public String getContentType() {
      return contentType;
   }

   public void setContentType(String contentType) {
      this.contentType = contentType;
   }
   
   
}
