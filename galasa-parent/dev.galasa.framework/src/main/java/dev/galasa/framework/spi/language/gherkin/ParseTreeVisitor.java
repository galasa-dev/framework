/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.spi.language.gherkin.parser.ParseToken;
import dev.galasa.framework.spi.teststructure.TestGherkinMethod;
import dev.galasa.framework.spi.teststructure.TestStructure;

/**
 * Visits a parse tree, and populates the meaning into a test structure.
 */
public class ParseTreeVisitor {

    ParseTreeVisitor() {
    }

    GherkinFeature visit(ParseToken rootNode) {
        return null;
    }
}
