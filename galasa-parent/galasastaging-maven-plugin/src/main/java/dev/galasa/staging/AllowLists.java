/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.staging;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
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
 * Build the allowlist files
 * 
 * @author mikebyls
 *
 */
@Mojo(name = "allowlists", requiresProject = false)
public class AllowLists extends AbstractMojo {

    @Parameter( defaultValue = "${settings}", readonly = true )
    private Settings settings;

    @Parameter(defaultValue = "${galasa.source.repo.nexus}", property = "sourceRepoNexus", required = true)
    private URL               sourceNexus;

    @Parameter(defaultValue = "${galasa.outputDir}", property = "outputDir", required = true)
    private File              outputDirectory;

    @Parameter(defaultValue = "${galasa.source.repo.id}", property = "sourceRepoId", required = false)
    private String            sourceId;

    @Parameter(defaultValue = "${galasa.source.repo.name}", property = "sourceRepoName", required = true)
    private String            sourceName;
    
    @Parameter(defaultValue = "${galasa.control.file}", property = "controlFile", required = false)
    private URL               controlFile;

    private CloseableHttpClient httpClient;
    private Header authNexus;

    private final Gson gson = new Gson();

    private ArrayList<Artifact> artifacts = new ArrayList<>();
    
    HashMap<Pattern, String> conversions = new HashMap<>();

    public void execute() throws MojoExecutionException {
        Path outDir = Paths.get(outputDirectory.toURI());
        try {
            Files.createDirectories(outDir);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to create target directory", e);
        }
        

        buildHttpClient();


        try {
            loadControlFile();
            
            retrieveKnowArtifacts();
            
            produceRawNexusArtifactList();

            produceRawMavenArtifactList();
            
            produceCondensedNexusArtifactList();

            report();

        } catch (UnsupportedEncodingException e) {
            throw new MojoExecutionException("Unexpected issue processing repositories", e);
        } 


    }

    private void produceRawMavenArtifactList() throws MojoExecutionException {
        Path outDir = Paths.get(outputDirectory.toURI());
        Path rawFile = outDir.resolve("maven-raw.txt");

        try (OutputStream os = Files.newOutputStream(rawFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)){
            try (Writer w = new OutputStreamWriter(os)) {
                for(Artifact artifact : this.artifacts) {
                    w.write(artifact.getNameVersion());
                    w.write('\n');
                }
            }
        } catch(Exception e) {
            throw new MojoExecutionException("Unable to create raw maven allowlist file", e);
        }
    }

    private void produceRawNexusArtifactList() throws MojoExecutionException {
        Path outDir = Paths.get(outputDirectory.toURI());
        Path rawFile = outDir.resolve("nexus-routing-raw.txt");

        try (OutputStream os = Files.newOutputStream(rawFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)){
            try (Writer w = new OutputStreamWriter(os)) {
                for(Artifact artifact : this.artifacts) {
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("^");
                    sb.append(convertArtifactToPath(artifact));
                    sb.append(".*");
                    sb.append("\n");
                    w.write(sb.toString());
                }
            }
        } catch(Exception e) {
            throw new MojoExecutionException("Unable to create raw nexus routing allowlist file", e);
        }
    }

    private void produceCondensedNexusArtifactList() throws MojoExecutionException {
        Path outDir = Paths.get(outputDirectory.toURI());
        Path rawFile = outDir.resolve("nexus-routing-condensed.txt");
        
        HashSet<String> paths = new HashSet<>();
        
        for(Artifact artifact : this.artifacts) {
            String path = convertArtifactToPath(artifact);
            
            for(Entry<Pattern, String> entry : conversions.entrySet()) {
                Matcher m = entry.getKey().matcher(path);
                if (m.matches()) {
                    path = entry.getValue();
                    break;
                }
            }
            
            if (!paths.contains(path)) {
                paths.add(path);
            }
        }
        
        ArrayList<String> sortedPaths = new ArrayList<>(paths);
        Collections.sort(sortedPaths);

        try (OutputStream os = Files.newOutputStream(rawFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)){
            try (Writer w = new OutputStreamWriter(os)) {
                for(String path : sortedPaths) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("^");
                    sb.append(path);
                    sb.append(".*");
                    sb.append("\n");
                    w.write(sb.toString());
                }
            }
        } catch(Exception e) {
            throw new MojoExecutionException("Unable to create condensed nexus routing allowlist file", e);
        }
    }
    
    private String convertArtifactToPath(Artifact artifact) {
        StringBuilder sb = new StringBuilder();
        String groupName = artifact.group.replaceAll("\\.", "/");
        sb.append("/");
        sb.append(groupName);
        sb.append("/");
        sb.append(artifact.artifact);
        sb.append("/");
        sb.append(artifact.version);
        sb.append("/");
       
        return sb.toString();
    }

    private void report() {
        getLog().info("The folllowing artifacts will be staged:-");
        if (artifacts.isEmpty()) {
            getLog().info("    No artifacts to be staged");
        } else {
            for(Artifact artifact : this.artifacts) {
                getLog().info("    " + artifact.getNameVersion());
            }

            getLog().info("");
            getLog().info("" + artifacts.size() + " artifacts");
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
    
    
    private void loadControlFile() throws MojoExecutionException {
        Yaml yaml = new Yaml();

        try {
            HashMap<String, Object> controlFile = yaml.load(this.controlFile.openStream());
            
            for(Entry<String, Object> entry : controlFile.entrySet()) {
                Pattern pattern = Pattern.compile(entry.getKey());
                conversions.put(pattern, (String) entry.getValue());
            }
        } catch(Exception e) {
            throw new MojoExecutionException("Unable to load control file",e);
        }

    }

}
