package dev.galasa.staging;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;

import dev.galasa.staging.json.ComponentResponse;
import dev.galasa.staging.json.Item;

/**
 * Clean artifacts from the staging repos
 * 
 * @author mikebyls
 *
 */
@Mojo(name = "cleanartifacts", requiresProject = false)
public class CleanArtifacts extends AbstractMojo {

    @Parameter( defaultValue = "${settings}", readonly = true )
    private Settings settings;

    @Parameter(defaultValue = "${galasa.source.repo.nexus}", property = "sourceRepoNexus", required = true)
    private URL               sourceNexus;

    @Parameter(defaultValue = "${galasa.source.repo.id}", property = "sourceRepoId", required = false)
    private String            sourceId;

    @Parameter(defaultValue = "${galasa.source.repo.name}", property = "sourceRepoName", required = true)
    private String            sourceName;

    @Parameter(defaultValue = "${galasa.target.repo}", property = "targetRepo", required = false)
    private URL               target;

    @Parameter(defaultValue = "${galasa.target.repo.id}", property = "targetRepoId", required = false)
    private String            targetId;

    @Parameter(defaultValue = "${galasa.control.file}", property = "controlFile", required = false)
    private URL               controlFile;

    @Parameter(defaultValue = "${galasa.dry.run}", property = "dryRun", required = false)
    private Boolean           dryRun;

    private CloseableHttpClient httpClient;
    private Header authNexus;
    private Header authTarget;

    private final Gson gson = new Gson();

    private ArrayList<Artifact> artifacts = new ArrayList<>();

    private HashSet<String> stageNames = new HashSet<>();
    private HashSet<String> intermediateNames = new HashSet<>();
    private HashSet<String> keepNames = new HashSet<>();
    
    private ArrayList<Artifact> stageArtifacts = new ArrayList<>();
    private ArrayList<Artifact> intermediateArtifacts = new ArrayList<>();
    private ArrayList<Artifact> duplicatedArtifacts = new ArrayList<>();
    private ArrayList<Artifact> unknownArtifacts = new ArrayList<>();

    public void execute() throws MojoExecutionException {

        if (this.dryRun == null) {
            this.dryRun = Boolean.FALSE;
        }

        buildHttpClient();
        loadControlFile();


        try {
            retrieveKnowArtifacts();

            processArtifacts();

            checkStageArtifactAgainstTarget();

            report();

            if (dryRun) {
                getLog().info("Not performing cleaning as dry run indicated");
            } else if (!this.unknownArtifacts.isEmpty()) {
                getLog().info("Not performing cleaning there are unknown artifacts listed");
            } else {
                if (this.duplicatedArtifacts.isEmpty()) {
                    getLog().info("There are no staging artifacts to clean");
                } else {
                    getLog().info("Deleting staged artifacts:-");
                    cleanArtifacts(this.duplicatedArtifacts);
                }
                if (this.intermediateArtifacts.isEmpty()) {
                    getLog().info("There are no intermediary artifacts to clean");
                } else {
                    getLog().info("Deleting intermediary artifacts:-");
                    cleanArtifacts(this.intermediateArtifacts);
                }
            }

        } catch (UnsupportedEncodingException e) {
            throw new MojoExecutionException("Unexpected issue processing repositories", e);
        } 


    }

    private void cleanArtifacts(ArrayList<Artifact> cleanArtifacts) throws MojoExecutionException {

        try {
            for(Artifact artifact : cleanArtifacts) {
                HttpDelete delete = new HttpDelete(this.sourceNexus.toString() + "/v1/components/" + artifact.id);
                if (authNexus != null) {
                    delete.addHeader(authNexus);
                }

                try (CloseableHttpResponse response = this.httpClient.execute(delete)) {
                    EntityUtils.consume(response.getEntity());

                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
                        throw new MojoExecutionException("Unexpected response from delete artifact " + artifact.getNameVersion() + " - " + response.getStatusLine().toString());
                    } else {
                        getLog().info("    Deleted " + artifact.getNameVersion());
                    }
                }
            }
        } catch(Exception e) {
            throw new MojoExecutionException("Unable to clean an artifact",e);
        }

    }

    private void report() {
        getLog().info("The folllowing artifacts should be staged:-");
        if (stageArtifacts.isEmpty()) {
            getLog().info("    No artifacts to be staged");
        } else {
            for(Artifact artifact : this.stageArtifacts) {
                getLog().info("    " + artifact.getNameVersion());
            }
        }
        getLog().info("");


        getLog().info("The folllowing artifacts will be cleaned, already exist in target:-");
        if (duplicatedArtifacts.isEmpty()) {
            getLog().info("    No artifacts to be cleaned");
        } else {
            for(Artifact artifact : this.duplicatedArtifacts) {
                getLog().info("    " + artifact.getNameVersion());
            }
        }
        getLog().info("");


        getLog().info("The folllowing intermediary artifacts will be cleaned:-");
        if (intermediateArtifacts.isEmpty()) {
            getLog().info("    No artifacts to be cleaned");
        } else {
            for(Artifact artifact : this.intermediateArtifacts) {
                getLog().info("    " + artifact.getNameVersion());
            }
        }
        getLog().info("");


        if (!this.unknownArtifacts.isEmpty()) {
            getLog().info("The folllowing artifacts are unknown and will prevent the clean from running:-");
            if (unknownArtifacts.isEmpty()) {
                getLog().info("    No unknown artifacts");
            } else {
                for(Artifact artifact : this.unknownArtifacts) {
                    getLog().info("    " + artifact.getName());
                }
            }
            getLog().info("");
        }
    }

    private void checkStageArtifactAgainstTarget() throws MojoExecutionException {
        Iterator<Artifact> iArtifact = stageArtifacts.iterator();
        while(iArtifact.hasNext()) {
            Artifact artifact = iArtifact.next();
            
            if (this.keepNames.contains(artifact.getName())) {
                continue;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("/");
            sb.append(artifact.group.replaceAll("\\.", "/"));
            sb.append("/");
            sb.append(artifact.artifact);
            sb.append("/");
            sb.append(artifact.version);
            sb.append("/");
            sb.append(artifact.artifact);
            sb.append("-");
            sb.append(artifact.version);
            sb.append(".pom");

            HttpGet getTargetArtifact = new HttpGet(this.target.toString() + sb.toString());
            if (authTarget != null) {
                getTargetArtifact.addHeader(authTarget);
            }
            try (CloseableHttpResponse response = httpClient.execute(getTargetArtifact)) {
                EntityUtils.consume(response.getEntity());

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    iArtifact.remove();
                    this.duplicatedArtifacts.add(artifact);
                } else if (statusCode == HttpStatus.SC_NOT_FOUND) {

                } else {
                    throw new MojoExecutionException("Unexpected response from target artifact " + getTargetArtifact.toString() + " - " + response.getStatusLine().toString());
                }

            } catch(Exception e) {
                throw new MojoExecutionException("Problem processing target artifact " + getTargetArtifact.toString(), e);
            }
        }

    }

    private void processArtifacts() {
        for(Artifact artifact : this.artifacts) {
            String name = artifact.getName();

            if (this.stageNames.contains(name)) {
                this.stageArtifacts.add(artifact);
            } else if (this.intermediateNames.contains(name)) {
                this.intermediateArtifacts.add(artifact);
            } else {
                this.unknownArtifacts.add(artifact);
            }
        }
    }

    private void loadControlFile() throws MojoExecutionException {
        Yaml yaml = new Yaml();

        try {
            HashMap<String, Object> controlFile = yaml.load(this.controlFile.openStream());

            Object oStage = controlFile.get("stage");
            loadControlFileSet(this.stageNames, oStage, "stage");
            Object oInter = controlFile.get("intermediary");
            loadControlFileSet(this.intermediateNames, oInter, "intermediary");
            Object oKeep = controlFile.get("keep");
            loadControlFileSet(this.keepNames, oKeep, "keep");
        } catch(Exception e) {
            throw new MojoExecutionException("Unable to load control file",e);
        }

    }

    private void loadControlFileSet(HashSet<String> set, Object oList, String property) throws MojoExecutionException {
        if (oList == null) {
            return;
        }
        
        if (!(oList instanceof List)) {
            throw new MojoExecutionException("Control file property " + property +" is not an array");
        }

        List<?> list = (List<?>) oList;

        for(Object entry : list) {
            if (!(entry instanceof String)) {
                throw new MojoExecutionException("Control file property " + property + " contains non string values");
            }

            set.add((String)entry);
        }

    }

    private void retrieveKnowArtifacts() throws UnsupportedEncodingException, MojoExecutionException {

        String continuationToken = "";
        while(true) {
            HttpGet get = new HttpGet(this.sourceNexus + "/v1/components?repository=" + URLEncoder.encode(sourceName, StandardCharsets.UTF_8.name()) + continuationToken);
            get.addHeader("Accept", "application/json");
            if (authNexus != null) {
                get.addHeader(authNexus);
            }

            try (CloseableHttpResponse response = httpClient.execute(get)) {
                String responseString = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new MojoExecutionException("Invalid response from Nexus - " + response.getStatusLine() + "\n" + responseString);
                }

                ComponentResponse componentResponse = gson.fromJson(responseString, ComponentResponse.class);

                if (componentResponse.items == null ||componentResponse.items.isEmpty()) {
                    break;
                }

                for(Item item : componentResponse.items) {
                    Artifact artifact = new Artifact();
                    artifact.id = item.id;
                    artifact.group = item.group;
                    artifact.artifact = item.name;
                    artifact.version = item.version;
                    artifact.repoId = item.id;
                    artifact.assets = item.assets;

                    this.artifacts.add(artifact);
                }

                if (componentResponse.continuationToken == null) {
                    break;
                }

                continuationToken = "&continuationToken=" + componentResponse.continuationToken;
            } catch(Exception e) {
                throw new MojoExecutionException("Problem accessing Nexus repository", e);
            }
        }

        Collections.sort(this.artifacts);

    }

    private void buildHttpClient() throws MojoExecutionException {
        if (this.sourceId != null) {
            UsernamePasswordCredentials credentials = getCredentials(this.sourceId);
            String creds = credentials.getUserName() + ":" + credentials.getPassword();
            String auth = Base64.getEncoder().encodeToString(creds.getBytes());
            authNexus = new BasicHeader("Authorization", "Basic " + auth);
            
        }

        if (this.targetId != null) {
            UsernamePasswordCredentials credentials = getCredentials(this.targetId);
            String creds = credentials.getUserName() + ":" + credentials.getPassword();
            String auth = Base64.getEncoder().encodeToString(creds.getBytes());
            authTarget = new BasicHeader("Authorization", "Basic " + auth);
        }

        this.httpClient = HttpClientBuilder.create().build();
    }

    private UsernamePasswordCredentials getCredentials(String id) throws MojoExecutionException {
        Server server = this.settings.getServer(id);
        if (server == null) {
            throw new MojoExecutionException("Unable to locate the server with an ID of '" +id +"'");
        }

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(server.getUsername(), server.getPassword());

        return credentials;
    }
}
