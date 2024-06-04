/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

public enum TokenType {

    // Terminal tokens
    FEATURE_START, // text contains feature description.
    SCENARIO_START, // text contains scenario name.
    SCENARIO_OUTLINE_START, // text contains scenario outline name.
    EXAMPLES_START, 
    STEP, // text contains the step text
    DATA_LINE, // text contains the table text, including | characters.
    END_OF_FILE,

    // Non-terminal tokens
    FEATURE,
    SCENARIO_OUTLINE,
    SCENARIO,
    SCENARIO_PART_LIST,
    SCENARIO_PART,
    DATA_TABLE_HEADER,
    DATA_TABLE_LINE,
    STEP_LIST
}