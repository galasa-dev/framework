/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import org.junit.Test;

import dev.galasa.framework.TestRunException;

public class TestGherkinParser {

    @Test
    public void testCanInstantiateParser() throws Exception {
        // <feature>
        // +-- FEATURE_START 
        // +-- <scenarioPartList>
        //     (no children)
        List<String> lines = List.of(
            "Feature: my feature"
        );
        LexicalScanner lexer = new GherkinLexicalScanner(lines);
        GherkinParser parser = new GherkinParser(lexer);

        ParseToken rootToken = parser.Parse();

        assertThat(rootToken).isNotNull();
        assertThat(rootToken.getType()).isEqualTo(TokenType.FEATURE);
        assertThat(rootToken.getChildren().size()).isEqualTo(2);

        // Some attributes should have been inherited from the first child...
        assertThat(rootToken.getLineNumber()).isEqualTo(1);
        assertThat(rootToken.getText()).isEqualTo("my feature");

        // The first child should be the FEATURE_START token.
        ParseToken shouldBeFeatureStart = rootToken.getChildren().get(0);
        assertThat(shouldBeFeatureStart.getType()).isEqualTo(TokenType.FEATURE_START);

        // The second child should be the scenario part list.
        ParseToken shouldBeScenarioPartList = rootToken.getChildren().get(1);
        assertThat(shouldBeScenarioPartList.getType()).isEqualTo(TokenType.SCENARIO_PART_LIST);
        // But that shouldn't have any children.
        assertThat(shouldBeScenarioPartList.getChildren()).isEmpty();
    }


    @Test
    public void testFailsIfMissingFeatureLine() throws Exception {

        List<String> lines = List.of(
            "No feature in file - should fail."
        );
        LexicalScanner lexer = new GherkinLexicalScanner(lines);
        GherkinParser parser = new GherkinParser(lexer);

        TestRunException ex = catchThrowableOfType( ()-> parser.Parse(), TestRunException.class );
        assertThat(ex)
            .hasMessageContaining("line 1")
            .hasMessageContaining("GHER002")
            .hasMessageContaining("Expected a `Feature:` token");
    }

    @Test
    public void testCanParseSimpleScenarioWithNoSteps() throws Exception {
        // <feature>
        // +-- FEATURE_START 
        // +-- <scenarioPartList> (1)
        //     +-- <<scenarioPart>
        //         +-- SCENARIO
        //             +-- <stepList>
        //                 (no children)
        //     +-- <scenarioPartList> (2)
        //         (no children)
        List<String> lines = List.of(
            "Feature: feature1",
            "  Scenario: scenario1"
        );
        LexicalScanner lexer = new GherkinLexicalScanner(lines);
        GherkinParser parser = new GherkinParser(lexer);

        ParseToken rootToken = parser.Parse();

        // We expect the 2nd child to be a scenario part list.
        ParseToken expectedScenarioPartList1 = rootToken.getChildren().get(1);
        assertThat(expectedScenarioPartList1.getType()).isEqualTo(TokenType.SCENARIO_PART_LIST);
        assertThat(expectedScenarioPartList1.getChildren()).hasSize(2);
        
        // The scenario part should be inside that.
        ParseToken expectedScenarioPart = expectedScenarioPartList1.getChildren().get(0);
        assertThat(expectedScenarioPart.getType()).isEqualTo(TokenType.SCENARIO);
        assertThat(expectedScenarioPart.getText()).isEqualTo("scenario1");

        // The scenario should have a scenario-start token.
        ParseToken expectedScenarioStart = expectedScenarioPart.getChildren().get(0);
        assertThat(expectedScenarioStart.getType()).isEqualTo(TokenType.SCENARIO_START);

        // It should also have a step list.
        ParseToken expectedStepList = expectedScenarioPart.getChildren().get(1);
        assertThat(expectedStepList.getType()).isEqualTo(TokenType.STEP_LIST);

        // That step list should be empty.
        assertThat(expectedStepList.getChildren()).isEmpty();
    }

    @Test
    public void testCanParseSimpleScenarioWithOneStep() throws Exception {
        // <feature> ("feature1")
        // +-- FEATURE_START ("feature1")
        // +-- <scenarioPartList> (1) ("scenario1")
        //     +-- <scenarioPart> ("scenario1")
        //         +-- SCENARIO ("scenario1")
        //             +-- SCENARIO_START ("scenario1")
        //             +-- <stepList>
        //                 +-- STEP ("Given step1")
        //                 +-- <stepList>
        //                     (no children)
        //     +-- <scenarioPartList> (2)
        //         (no children)
        List<String> lines = List.of(
            "Feature: feature1",
            "  Scenario: scenario1",
            "      Given step1"
        );
        LexicalScanner lexer = new GherkinLexicalScanner(lines);
        GherkinParser parser = new GherkinParser(lexer);

        ParseToken rootToken = parser.Parse();

        // We expect the 2nd child to be a scenario part list.
        ParseToken expectedScenarioPartList1 = rootToken.getChildren().get(1);
        assertThat(expectedScenarioPartList1.getType()).isEqualTo(TokenType.SCENARIO_PART_LIST);
        assertThat(expectedScenarioPartList1.getChildren()).hasSize(2);
        
        // The scenario part should be inside that.
        ParseToken expectedScenarioPart = expectedScenarioPartList1.getChildren().get(0);
        assertThat(expectedScenarioPart.getType()).isEqualTo(TokenType.SCENARIO);
        assertThat(expectedScenarioPart.getText()).isEqualTo("scenario1");

        // The scenario should have a scenario-start token.
        ParseToken expectedScenarioStart = expectedScenarioPart.getChildren().get(0);
        assertThat(expectedScenarioStart.getType()).isEqualTo(TokenType.SCENARIO_START);

        // It should also have a step list.
        ParseToken expectedStepList = expectedScenarioPart.getChildren().get(1);
        assertThat(expectedStepList.getType()).isEqualTo(TokenType.STEP_LIST);

        // That step list should be empty.
        ParseToken expectedStep1 = expectedStepList.getChildren().get(0);
        assertThat(expectedStep1.getType()).isEqualTo(TokenType.STEP);
        assertThat(expectedStep1.getText()).isEqualTo("Given step1");

        // We expect that step list to have a step list child also... which has no children itself.
        ParseToken expectedStepList2 = expectedStepList.getChildren().get(1);
        assertThat(expectedStepList2.getType()).isEqualTo(TokenType.STEP_LIST);
        assertThat(expectedStepList2.getChildren()).isEmpty();
    }
    

    @Test
    public void testCanParseSimpleScenarioWithTwoSteps() throws Exception {
        // <feature> ("feature1")
        // +-- FEATURE_START ("feature1")
        // +-- <scenarioPartList> (1) ("scenario1")
        //     +-- <scenarioPart> ("scenario1")
        //         +-- SCENARIO ("scenario1")
        //             +-- SCENARIO_START ("scenario1")
        //             +-- <stepList>
        //                 +-- STEP ("Given step1")
        //                 +-- <stepList>
        //                     +-- STEP ("Given step1")
        //                     +-- <stepList>
        //                         (no children)
        //     +-- <scenarioPartList> (2)
        //         (no children)
        List<String> lines = List.of(
            "Feature: feature1",
            "  Scenario: scenario1",
            "      Given step1",
            "      And step2"
        );
        LexicalScanner lexer = new GherkinLexicalScanner(lines);
        GherkinParser parser = new GherkinParser(lexer);

        ParseToken rootToken = parser.Parse();

        ParseToken expectedScenarioPartList1 = rootToken.getChildren().get(1);
        ParseToken expectedScenarioPart = expectedScenarioPartList1.getChildren().get(0);
        ParseToken expectedStepList = expectedScenarioPart.getChildren().get(1);

        ParseToken expectedStepList2 = expectedStepList.getChildren().get(1);
        assertThat(expectedStepList2.getType()).isEqualTo(TokenType.STEP_LIST);

        ParseToken expectedStep2 = expectedStepList2.getChildren().get(0);
        assertThat(expectedStep2.getType()).isEqualTo(TokenType.STEP);
        assertThat(expectedStep2.getText()).isEqualTo("And step2");

        ParseToken expectedStepList3 = expectedStepList2.getChildren().get(1);
        assertThat(expectedStepList3.getType()).isEqualTo(TokenType.STEP_LIST);
        assertThat(expectedStepList3.getChildren()).isEmpty();
    }
}
