package dev.galasa.staging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

import com.google.gson.Gson;

import dev.galasa.staging.json.Asset;
import dev.galasa.staging.json.ComponentResponse;
import dev.galasa.staging.json.Item;

/**
 * Extracts the artifacts into a directory
 * 
 * @author Michael Baylis
 *
 */
@Mojo(name = "extractartifacts", 
requiresProject = false)
public class ExtractArtifacts extends AbstractMojo {

    @Parameter( defaultValue = "${settings}", readonly = true )
    private Settings settings;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${galasa.output.directory}", property = "outputDir", required = true)
    private File         outputDirectory;

    @Parameter(defaultValue = "${galasa.source.repo.nexus}", property = "sourceRepoNexus", required = true)
    private URL               sourceNexus;

    @Parameter(defaultValue = "${galasa.source.repo.id}", property = "sourceRepoId", required = false)
    private String            sourceId;

    @Parameter(defaultValue = "${galasa.source.repo.name}", property = "sourceRepoName", required = true)
    private String            sourceName;


    private CloseableHttpClient httpClient;
    private Header authNexus;
    private Header authTarget;

    private final Gson gson = new Gson();

    private ArrayList<Artifact> artifacts = new ArrayList<>();

    public void execute() throws MojoExecutionException, MojoFailureException {

        buildHttpClient();

        try {
            retrieveKnowArtifacts();

            report();

            if (this.artifacts.isEmpty()) {
                getLog().info("There are no extracted artifacts");
            } else {
                getLog().info("Extracted Artifacts:-");
                stageArtifacts(this.artifacts);
            }

        } catch (UnsupportedEncodingException e) {
            throw new MojoExecutionException("Unexpected issue processing repositories", e);
        } 

    }


    private void stageArtifacts(ArrayList<Artifact> stageArtifacts) throws MojoExecutionException {
        Path outDir = Paths.get(outputDirectory.toURI());
        try {
            Files.createDirectories(outDir);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to create target directory", e);
        }

        try {
            for(Artifact artifact : stageArtifacts) {
                getLog().info("     Processing " + artifact.getNameVersion());

                if (artifact.assets != null && !artifact.assets.isEmpty()) {
                    for(Asset asset : artifact.assets) {
                        HttpGet get = new HttpGet(asset.downloadUrl);
                        if (authNexus != null) {
                            get.addHeader(authNexus);
                        }  

                        Path targetPath = outDir.resolve(asset.path);
                        
                        Files.createDirectories(targetPath.getParent());

                        try(CloseableHttpResponse getResponse = this.httpClient.execute(get)) {
                            if (getResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                                throw new MojoExecutionException("Unexpected response from get artifact " + artifact.getNameVersion() + " - " + getResponse.getStatusLine().toString());
                            }

                            InputStream getInputStream = getResponse.getEntity().getContent();

                            try (OutputStream outputStream = Files.newOutputStream(targetPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                                IOUtils.copy(getInputStream, outputStream);
                            }

                            getLog().info("        " + asset.path);
                        }
                    }
                }
            }
        } catch(Exception e) {
            throw new MojoExecutionException("Unable to copy an artifact",e);
        }

    }



    private void report() {
        getLog().info("The folllowing artifacts will be extracted:-");
        if (artifacts.isEmpty()) {
            getLog().info("    No artifacts to be extracted");
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
