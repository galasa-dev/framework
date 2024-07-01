/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import static org.assertj.core.api.Assertions.*;

import dev.galasa.framework.spi.language.gherkin.parser.*;
import java.util.List;
import org.junit.Test;

public class TestParseVisitor {

    @Test
    public void testCanInstantiateVisitor() {
        new ParseTreeVisitor();
    }

    @Test
    public void TestAnEmptyFeatureCreatesNoTestMethods() throws Exception {
        // Given...
        List<String> lines = List.of(
            "Feature: my feature"
        );
        ParseToken rootToken = getParseTreeRootToken(lines);

        ParseTreeVisitor visitor = new ParseTreeVisitor();

        // When...
        GherkinFeature feature = visitor.visit(rootToken);

        // Then...

    }

    private ParseToken getParseTreeRootToken( List<String> lines) throws Exception {
        LexicalScanner lexer = new GherkinLexicalScanner(lines);
        GherkinParser parser = new GherkinParser(lexer);
        ParseToken rootToken = parser.Parse();
        return rootToken ;
    }
}
