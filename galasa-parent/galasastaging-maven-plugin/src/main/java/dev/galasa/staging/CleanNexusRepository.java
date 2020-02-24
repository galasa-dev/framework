package dev.galasa.staging;

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

import com.google.gson.Gson;

import dev.galasa.staging.json.ComponentResponse;
import dev.galasa.staging.json.Item;

/**
 * Deletes all artifacts from a repository
 * 
 * @author mikebyls
 *
 */
@Mojo(name = "cleannexusrepo", requiresProject = false)
public class CleanNexusRepository extends AbstractMojo {

    @Parameter( defaultValue = "${settings}", readonly = true )
    private Settings settings;

    @Parameter(defaultValue = "${galasa.source.repo.nexus}", property = "sourceRepoNexus", required = true)
    private URL               sourceNexus;

    @Parameter(defaultValue = "${galasa.source.repo.id}", property = "sourceRepoId", required = false)
    private String            sourceId;

    @Parameter(defaultValue = "${galasa.source.repo.name}", property = "sourceRepoName", required = true)
    private String            sourceName;

    private CloseableHttpClient httpClient;
    private Header authNexus;

    private final Gson gson = new Gson();

    private ArrayList<Artifact> artifacts = new ArrayList<>();

    public void execute() throws MojoExecutionException {

        buildHttpClient();


        try {
            retrieveKnowArtifacts();

            getLog().info("Deleting artifacts:-");
            cleanArtifacts(this.artifacts);
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
