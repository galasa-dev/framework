/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import org.junit.Test;

public class TestGherkinParser {

    @Test
    public void testCanInstantiateParser() throws Exception {
        List<String> lines = List.of(
            "Feature: my feature"
        );
        LexicalScanner lexer = new GherkinLexicalScanner(lines);
        GherkinParser parser = new GherkinParser(lexer);

        ParseToken rootToken = parser.Parse();
        assertThat(rootToken).isNotNull();
    }
    
}
