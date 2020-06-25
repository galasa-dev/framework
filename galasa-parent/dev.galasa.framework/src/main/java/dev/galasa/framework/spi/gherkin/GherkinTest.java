package dev.galasa.framework.spi.gherkin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.IRun;

public class GherkinTest {

    private List<GhrekinMethod> methods;
    private URI gherkinUri;

    private String testName;
    private List<String> comments;

    public GherkinTest(IRun run) throws TestRunException {
        this.methods = new ArrayList<>();
        this.comments = new ArrayList<>();

        try {
            gherkinUri = new URI(run.getGherkin());

            if (gherkinUri.getScheme().equals("file")) {
                File gherkinFile = new File(gherkinUri);
                BufferedReader br = new BufferedReader(new FileReader(gherkinFile));

                String line;
                GhrekinMethod currentMethod = null;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if(line.isEmpty()) {
                        continue;
                    }

                    if(line.startsWith("Feature:")) {
                        testName = line.substring(8).trim();
                    } else if (line.startsWith("Scenario:")) {
                        if(currentMethod != null) {
                            methods.add(currentMethod);
                        }
                        currentMethod = new GhrekinMethod(line.substring(9).trim());
                    } else if(currentMethod != null) {
                        currentMethod.addStatement(line);
                    } else {
                        this.comments.add(line);
                    }
                }
                if(currentMethod != null) {
                    methods.add(currentMethod);
                }
                br.close();
            } else {
                throw new TestRunException("Gherkin URI scheme " + gherkinUri.getScheme() + "is not supported");
            }
        } catch (URISyntaxException e) {
            throw new TestRunException("Unable to parse gherkin test URI", e);
        } catch (FileNotFoundException e) {
            throw new TestRunException("Unable to find gherkin test file", e);
        } catch (IOException e) {
            throw new TestRunException("Error reading gherkin test file", e);
        }
    }

    public String getName() {
        return this.testName;
    }

    public List<GhrekinMethod> getMethods() {
        return this.methods;
    }

}