package dev.galasa.staging;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
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

import com.google.gson.Gson;

import dev.galasa.staging.json.Asset;
import dev.galasa.staging.json.ComponentResponse;
import dev.galasa.staging.json.Item;

/**
 * Clean artifacts from the staging repos
 * 
 * @author mikebyls
 *
 */
@Mojo(name = "stageartifacts", requiresProject = false)
public class StageArtifacts extends AbstractMojo {

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

    @Parameter(defaultValue = "${galasa.dry.run}", property = "dryRun", required = false)
    private Boolean           dryRun;

    private CloseableHttpClient httpClient;
    private Header authNexus;
    private Header authTarget;

    private final Gson gson = new Gson();

    private ArrayList<Artifact> artifacts = new ArrayList<>();

    public void execute() throws MojoExecutionException {

        if (this.dryRun == null) {
            this.dryRun = Boolean.FALSE;
        }

        buildHttpClient();


        try {
            retrieveKnowArtifacts();

            report();

            if (dryRun) {
                getLog().info("Not performing staging as dry run indicated");
            } else {
                if (this.artifacts.isEmpty()) {
                    getLog().info("There are no staging artifacts");
                } else {
                    getLog().info("Staged Artifacts:-");
                    stageArtifacts(this.artifacts);
                }
            }

        } catch (UnsupportedEncodingException e) {
            throw new MojoExecutionException("Unexpected issue processing repositories", e);
        } 


    }

    private void stageArtifacts(ArrayList<Artifact> stageArtifacts) throws MojoExecutionException {

        try {
            for(Artifact artifact : stageArtifacts) {
                getLog().info("     Processing " + artifact.getNameVersion());

                if (artifact.assets != null && !artifact.assets.isEmpty()) {
                    for(Asset asset : artifact.assets) {
//                        if (asset.path.endsWith(".sha1") || asset.path.endsWith(".md5")) {
//                            continue;  // Do not send the hashes
//                        }


                        HttpGet get = new HttpGet(asset.downloadUrl);
                        if (authNexus != null) {
                            get.addHeader(authNexus);
                        }  

                        HttpPut put = new HttpPut(this.target.toString() + "/" + asset.path);
                        if (authTarget != null) {
                            put.addHeader(authTarget);
                        }  

                        try(CloseableHttpResponse getResponse = this.httpClient.execute(get)) {
                            if (getResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                                throw new MojoExecutionException("Unexpected response from get artifact " + artifact.getNameVersion() + " - " + getResponse.getStatusLine().toString());
                            }
                            
                            InputStream getInputStream = getResponse.getEntity().getContent();
                            
                            InputStreamEntity putEntity = new InputStreamEntity(getInputStream);
                            put.setEntity(putEntity);                            
                            try(CloseableHttpResponse putResponse = this.httpClient.execute(put)) {
                                if (putResponse.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                                    throw new MojoExecutionException("Unexpected response from put artifact " + artifact.getNameVersion() + " - " + putResponse.getStatusLine().toString());
                                }
                                
                                EntityUtils.consume(putResponse.getEntity());
                                
                                getLog().info("        " + asset.path);
                            }
                        }
                    }
                }
            }
        } catch(Exception e) {
            throw new MojoExecutionException("Unable to clean an artifact",e);
        }

    }

    private void report() {
        getLog().info("The folllowing artifacts will be staged:-");
        if (artifacts.isEmpty()) {
            getLog().info("    No artifacts to be staged");
        } else {
            for(Artifact artifact : this.artifacts) {
                getLog().info("    " + artifact.getNameVersion());
            }
        }
        getLog().info("");
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
