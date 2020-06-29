package dev.galasa.staging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Change the version numbers of Galasa artifacts
 * 
 * @author Michael Baylis
 *
 */
@Mojo(name = "versionupdate", requiresProject = false)
public class VersionUpdate extends AbstractMojo {

    @Parameter(defaultValue = "${galasa.target.directory}", property = "targetDirectory", required = true)
    private File               targetDirectory;

    @Parameter(defaultValue = "${galasa.from.version}", property = "fromVersion", required = true)
    private String            fromVersion;

    @Parameter(defaultValue = "${galasa.from.snapshot}", property = "fromSnapshot", required = true)
    private boolean           fromSnapshot;

    @Parameter(defaultValue = "${galasa.to.version}", property = "toVersion", required = true)
    private String            toVersion;

    @Parameter(defaultValue = "${galasa.to.snapshot}", property = "toSnapshot", required = true)
    private boolean           toSnapshot;

    @Parameter(defaultValue = "${galasa.dry.run}", property = "dryRun", required = false)
    private Boolean           dryRun;

    private Path parentPath;
    private Pattern pomPattern;
    private Pattern openapiPattern;
    private Pattern manifestPattern;
    private Pattern featurePattern;
    private Pattern categoryPattern;
    private String  mavenFrom;
    private String  mavenTo;
    private boolean updateEclipse = false;

    public void execute() throws MojoExecutionException {

        if (this.dryRun == null) {
            this.dryRun = Boolean.FALSE;
        }

        parentPath = Paths.get(targetDirectory.toURI());

        // Calculate version Strings
        mavenFrom = fromVersion;
        if (fromSnapshot) {
            mavenFrom = mavenFrom + "-SNAPSHOT";
        }

        mavenTo = toVersion;
        if (toSnapshot) {
            mavenTo = mavenTo + "-SNAPSHOT";
        }

        if (!fromVersion.equals(toVersion)) {
            updateEclipse = true;
        }

        // Create regex patterns to search form
        pomPattern = Pattern.compile("\\Q<artifactId>\\E(galasa-[a-z0-9-\\.]+|galasastaging-maven-plugin|galasautils-maven-plugin|runtime|dev\\.galasa[a-z0-9-\\.]*)\\Q</artifactId>\\E\\s+\\Q<version>\\E(\\Q" + mavenFrom + "\\E)\\Q</version>\\E");
        openapiPattern = Pattern.compile("\\Qversion: \\E(\\Q" + mavenFrom + "\\E)");
        manifestPattern = Pattern.compile("^\\QBundle-Version: \\E(\\Q" + fromVersion + "\\E).qualifier", Pattern.MULTILINE);
        featurePattern = Pattern.compile("<(feature|plugin)\\s+\\Qid=\\E\\\"(\\Qdev.galasa\\E[a-z0-9-\\\\.]*)\\\"\\s+\\Qversion=\\E\\\"(\\Q" + fromVersion + "\\E)\\Q.qualifier\\E\\\"");
        categoryPattern = Pattern.compile("(\\Q" + fromVersion + "\\E)\\Q.qualifier\\E");


        try {
            processDirectory(targetDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Problem processing files", e);
        }
    }

    private void processDirectory(File directory) throws IOException {
        for(File file : directory.listFiles()) {
            if (file.isHidden()) {
                continue;
            }

            if (file.isDirectory()) {
                if ("target".equals(file.getName())) {
                    continue;
                }
                processDirectory(file);
                continue;
            }

            if ("pom.xml".equals(file.getName())) {
                processPomXml(file);
                continue;
            }

            if ("pom-example.xml".equals(file.getName())) {
                processPomXml(file);
                continue;
            }

            if ("openapi.yaml".equals(file.getName())) {
                processOpenapi(file);
            }

            if ("MANIFEST.MF".equals(file.getName())) {
                if (updateEclipse) {
                    processManifest(file); 
                }
            }

            if ("feature.xml".equals(file.getName())) {
                if (updateEclipse) {
                    processFeature(file);
                }
            }
            if ("category.xml".equals(file.getName())) {
                if (updateEclipse) {
                    processCategory(file);
                }
            }
        }
    }

    private void processPomXml(File file) throws IOException {
        String pom = IOUtils.toString(file.toURI(), StandardCharsets.UTF_8);

        boolean nameWritten = false;        
        boolean found = true;
        while(found) {
            found = false;
            Matcher matcher = pomPattern.matcher(pom);

            if (matcher.find()) {
                String artifactId = matcher.group(1);
                String version = matcher.group(2);

                int versionStart = matcher.start(2);
                int versionEnd   = matcher.end(2);

                pom = pom.substring(0, versionStart) + mavenTo + pom.substring(versionEnd);
                found = true;

                if (!nameWritten) {
                    Path path = Paths.get(file.toURI());
                    Path relative = parentPath.relativize(path);
                    getLog().info("File " + relative.toString());
                    nameWritten = true;
                }
                getLog().info("    Maven artifact " + artifactId + " version changed from " + version + " to " + mavenTo);
            }
        }

        if (!dryRun) {
            IOUtils.write(pom.getBytes(StandardCharsets.UTF_8), new FileOutputStream(file));
        }
    }

    private void processOpenapi(File file) throws IOException {
        String openapi = IOUtils.toString(file.toURI(), StandardCharsets.UTF_8);

        Matcher matcher = openapiPattern.matcher(openapi);
        if (matcher.find()) {
            String version = matcher.group(1);

            int versionStart = matcher.start(1);
            int versionEnd   = matcher.end(1);

            openapi = openapi.substring(0, versionStart) + mavenTo + openapi.substring(versionEnd);
            Path path = Paths.get(file.toURI());
            Path relative = parentPath.relativize(path);
            getLog().info("File " + relative.toString());
            getLog().info("    OpenApi version changed from " + version + " to " + mavenTo);
        }

        if (!dryRun) {
            IOUtils.write(openapi.getBytes(StandardCharsets.UTF_8), new FileOutputStream(file));
        }
    }

    private void processManifest(File file) throws IOException {
        String manifest = IOUtils.toString(file.toURI(), StandardCharsets.UTF_8);

        Matcher matcher = manifestPattern.matcher(manifest);
        if (matcher.find()) {
            String version = matcher.group(1);

            int versionStart = matcher.start(1);
            int versionEnd   = matcher.end(1);

            manifest = manifest.substring(0, versionStart) + toVersion + manifest.substring(versionEnd);
            Path path = Paths.get(file.toURI());
            Path relative = parentPath.relativize(path);
            getLog().info("File " + relative.toString());
            getLog().info("    Manifest version changed from " + version + " to " + toVersion);
        }

        if (!dryRun) {
            IOUtils.write(manifest.getBytes(StandardCharsets.UTF_8), new FileOutputStream(file));
        }
    }

    private void processFeature(File file) throws IOException {
        String feature = IOUtils.toString(file.toURI(), StandardCharsets.UTF_8);

        boolean nameWritten = false;        
        boolean found = true;
        while(found) {
            found = false;
            Matcher matcher = featurePattern.matcher(feature);

            if (matcher.find()) {
                String artifactId = matcher.group(2);
                String version = matcher.group(3);

                int versionStart = matcher.start(3);
                int versionEnd   = matcher.end(3);

                feature = feature.substring(0, versionStart) + toVersion + feature.substring(versionEnd);
                found = true;

                if (!nameWritten) {
                    Path path = Paths.get(file.toURI());
                    Path relative = parentPath.relativize(path);
                    getLog().info("File " + relative.toString());
                    nameWritten = true;
                }
                getLog().info("    " + matcher.group(1) + " " + artifactId + " version changed from " + version + " to " + toVersion);
            }
        }

        if (!dryRun) {
            IOUtils.write(feature.getBytes(StandardCharsets.UTF_8), new FileOutputStream(file));
        }
    }

    private void processCategory(File file) throws IOException {
        String feature = IOUtils.toString(file.toURI(), StandardCharsets.UTF_8);

        boolean nameWritten = false;        
        boolean found = true;
        while(found) {
            found = false;
            Matcher matcher = categoryPattern.matcher(feature);

            if (matcher.find()) {
                String version = matcher.group(1);
                int versionStart = matcher.start(1);
                int versionEnd   = matcher.end(1);

                feature = feature.substring(0, versionStart) + toVersion + feature.substring(versionEnd);
                found = true;

                if (!nameWritten) {
                    Path path = Paths.get(file.toURI());
                    Path relative = parentPath.relativize(path);
                    getLog().info("File " + relative.toString());
                    nameWritten = true;
                }
                getLog().info("    version changed from " + version + " to " + toVersion);
            }
        }

        if (!dryRun) {
            IOUtils.write(feature.getBytes(StandardCharsets.UTF_8), new FileOutputStream(file));
        }
    }


}
