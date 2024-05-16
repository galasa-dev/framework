/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import java.net.URI;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import dev.galasa.framework.TestRunException;

public class GherkinFileReader implements IGherkinFileReader {
    
    @Override
    public List<String> readLines(URI gherkinUri) throws TestRunException {
        List<String> lines ;
        try {
            File gherkinFile = new File(gherkinUri);
            String content = FileUtils.readFileToString(gherkinFile,"UTF-8");
            lines = IOUtils.readLines(new FileReader(gherkinFile));
        } catch (IOException e) {
            throw new TestRunException("Unable to find gherkin test file", e);
        }
        return lines ;
    }
}
