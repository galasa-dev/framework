/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.TestRunException;

public class GherkinParser {
    
    private final LexicalScanner lex ;
    private final Stack<ParseToken> tokens = new Stack<ParseToken>();

    private final static Log logger = LogFactory.getLog(GherkinParser.class);

    public GherkinParser(LexicalScanner lex) {
        this.lex = lex;
    }

    public void shift(ParseToken token) {
        logger.debug("Parser: shifting token "+token.toString());
        tokens.add(token);
    }

    public void reduce(int tokensToReduce, ParseToken parentToken) {
        logger.debug("Parser: reducing "+Integer.toString(tokensToReduce));
        // Pull the last n elements off the parse stack.
        List<ParseToken> children = new ArrayList<ParseToken>();
        while(tokensToReduce>0) {
            ParseToken token = tokens.pop();
            children.add(0,token);
            tokensToReduce -=1;
        }

        // Set them into the parent token.
        parentToken.addChildren(children);

        parentTokenInheritsChildAttributes(parentToken);

        for(ParseToken childToken: parentToken.children) {
            logger.debug("Parser:   child: "+childToken.toString());
        }

        shift(parentToken);
    }

    private void parentTokenInheritsChildAttributes( ParseToken parentToken ) {
        List<ParseToken> children = parentToken.getChildren();
        if (children.size() > 0 ) {
            ParseToken firstChild = children.get(0);
            parentToken.setLineNumber( firstChild.getLineNumber() );
            parentToken.setText( firstChild.getText());
        }
    }

    // Returns a tree of parse token nodes as the abstract syntax tree representation.

    // We are here: (at the @ character):  
    // <feature> := @ FEATURE_START <scenarioPartList> END_OF_FILE
    public ParseToken Parse() throws TestRunException {
        ParseToken token = lex.getNextToken();
        switch(token.getType()) {

            case FEATURE_START:
                shift(token);

                // <feature> := FEATURE_START @ <scenarioPartList> END_OF_FILE
                parseScenarioPartList();
                
                // <feature> := FEATURE_START <scenarioPartList> @ END_OF_FILE
                ParseToken token2 = lex.getNextToken();
                if (token2.getType()!=TokenType.END_OF_FILE) {
                    String msg = MessageFormat.format("GHER001: Unexpected token {0} on line {1}",token2.getType(),token2.getLineNumber());
                    error(msg);
                }

                // <feature> := FEATURE_START <scenarioPartList> END_OF_FILE @
                reduce(2,new ParseToken(TokenType.FEATURE, token.getText(), token.getLineNumber()));
                break;

            default:
                String msg = MessageFormat.format("GHER002: Expected a `Feature:` token on line {0}",Integer.toString(token.getLineNumber()));
                error(msg);
                break;
        }
        ParseToken featureToken = tokens.pop();
        return featureToken ;
    }

    // We are here: (at the @ character):  
    // <scenarioPartList> := @ END_OF_FILE
    //                     | @ <scenarioPart> <scenarioPartList>
    //            
    private void parseScenarioPartList() throws TestRunException {
        ParseToken token = lex.getNextToken();
        switch(token.getType()) {
            case END_OF_FILE:
                reduce(0,new ParseToken(TokenType.SCENARIO_PART_LIST,"",token.getLineNumber()));
                break;

            case SCENARIO_START:
            case SCENARIO_OUTLINE_START:
                // <scenarioPartList> := @ <scenarioPart> <scenarioPartList>
                lex.pushBackToken(token);
                parseScenarioPart();
                // <scenarioPartList> := <scenarioPart> @ <scenarioPartList>
                parseScenarioPartList();
                // <scenarioPartList> := <scenarioPart> <scenarioPartList> @
                reduce(2, new ParseToken(TokenType.SCENARIO_PART_LIST, ""));
                break;

            default:
                String msg = MessageFormat.format("GHER003: Unexpected token {0} at line {1}", token.getType(), Integer.toString(token.getLineNumber()));
                error(msg);
                break;
        }
    }

    // <scenarioPart> := @ <scenarioOutline>
    //                 | @ <scenario>
    private void parseScenarioPart() throws TestRunException {
        ParseToken token = lex.getNextToken();
        switch(token.getType()) {
            
            case SCENARIO_START:
                // <scenarioPart> := @ <scenario>
                lex.pushBackToken(token);
                parseScenario();
                break;

            case SCENARIO_OUTLINE_START:
                // <scenarioPart> := @ <scenarioOutline>
                lex.pushBackToken(token);
                parseScenarioOutline();
                reduce(1,new ParseToken(TokenType.SCENARIO_PART, ""));
                break;

            default:
                String msg = MessageFormat.format("GHER004: Unexpected token {0} at line {1}", token.getType(), Integer.toString(token.getLineNumber()));
                error(msg);
                break;
        }
    }

    // <scenarioOutline> := @ SCENARIO_START <stepList> 
    private void parseScenario() throws TestRunException {
        ParseToken token = lex.getNextToken();
        switch(token.getType()) {
            
            case SCENARIO_START:
                // <scenarioOutline> := SCENARIO_START @ <stepList> 
                shift(token);
                parseStepList();
                
                // <scenarioOutline> := SCENARIO_OUTLINE_START <stepList> EXAMPLES_START <dataTable> @
                reduce(2,new ParseToken(TokenType.SCENARIO, ""));
                break;

            default:
                String msg = MessageFormat.format("GHER005: Unexpected token {0} at line {1}", token.getType(), Integer.toString(token.getLineNumber()));
                error(msg);
                break;
        }
    }

    //  <scenarioOutline> := @ SCENARIO_OUTLINE_START <stepList> EXAMPLES_START <dataTable>
    private void parseScenarioOutline() throws TestRunException {
        ParseToken token = lex.getNextToken();
        switch(token.getType()) {
            
            case SCENARIO_OUTLINE_START:
                // <scenarioOutline> := SCENARIO_OUTLINE_START @ <stepList> EXAMPLES_START <dataTable>
                shift(token);
                parseStepList();
                
                // <scenarioOutline> := SCENARIO_OUTLINE_START <stepList> @ EXAMPLES_START <dataTable>
                ParseToken token2 = lex.getNextToken();
                if (token.getType() != TokenType.EXAMPLES_START) {
                    String msg = MessageFormat.format("GHER006: Unexpected token {0} at line {1}", token2.getType(), Integer.toString(token2.getLineNumber()));
                    error(msg);
                }

                // <scenarioOutline> := SCENARIO_OUTLINE_START <stepList> EXAMPLES_START @ <dataTable>
                // TODO: parseDataTable();

                // <scenarioOutline> := SCENARIO_OUTLINE_START <stepList> EXAMPLES_START <dataTable> @
                reduce(4,new ParseToken(TokenType.SCENARIO_OUTLINE, ""));
                break;

            default:
                String msg = MessageFormat.format("GHER007: Unexpected token {0} at line {1}", token.getType(), Integer.toString(token.getLineNumber()));
                error(msg);
                break;
        }
    }

    // <stepList> := @ null
    //             | @ STEP <stepList>
    private void parseStepList() throws TestRunException {
        ParseToken token = lex.getNextToken();
        switch(token.getType()) {
            case SCENARIO_START:
            case SCENARIO_OUTLINE_START:
            case END_OF_FILE:
                // <stepList> := @ null
                lex.pushBackToken(token);
                reduce(0,new ParseToken(TokenType.STEP_LIST,"",token.getLineNumber()));
                break;

                 
            case STEP:
                shift(token);
                // <stepList> := STEP @ <stepList>

                parseStepList();
                // <stepList> := STEP <stepList> @

                reduce(2, new ParseToken(TokenType.STEP_LIST, ""));
                break;

            default:
                String msg = MessageFormat.format("GHER008: Unexpected token {0} at line {1}", token.getType(), Integer.toString(token.getLineNumber()));
                error(msg);
                break;
        }
    }

    private void error(String message) throws TestRunException {
        throw new TestRunException(message);
    }

}
