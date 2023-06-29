/*
 * Copyright contributors to the Galasa project
 */
package main.java.dev.galasa.framework.spi.language.gherkin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GherkinVariables {


    private final static Pattern exampleInstancePattern = Pattern.compile("\\|\\ *([a-zA-Z0-9 ]+)");

    private ArrayList<String> columns = new ArrayList<>();

    public HashMap<String, ArrayList<String>> variables = new HashMap<>();

    public void processHeaderLine(String headerLine) {
        ArrayList<String>elements = splitExampleData(headerLine);
        for(String element : elements) {
            this.variables.put(element.trim(), new ArrayList<String>());
            this.columns.add(element.trim());
        }
    }

    public void processDataLine(String dataLine) {
        ArrayList<String>elements = splitExampleData(dataLine);
        int column = 0;
        for(String element : elements) {
            //get the key for this column
            String key = this.columns.get(column);
            this.variables.get(key).add(element.trim());
            column++;
        }
    }

    private ArrayList<String> splitExampleData(String line) {
        ArrayList<String> bits = new ArrayList<String>();
        Matcher exampleInstanceMatcher = exampleInstancePattern.matcher(line);
        while(exampleInstanceMatcher.find()) {
            bits.add(exampleInstanceMatcher.group(1));
        }
        return bits;
    }

    public HashMap<String,ArrayList<String>> getVariables(){
        return this.variables;
    }

    public int getNumberOfInstances() {
        return variables.entrySet().iterator().next().getValue().size();
    }

    public HashMap<String,String> getVariableInstance(int instance) {
        HashMap<String,String> result = new HashMap<>();
        for(String key : this.variables.keySet()) {
            result.put(key, this.variables.get(key).get(instance));
        }
        return result;
    }

    public HashMap<String,Object> getVariablesOriginal(){
        HashMap<String, Object> result = new HashMap<>();
        for(String s : getVariables().keySet()) {
            result.put(s, getVariables().get(s));
        }
        return result;
    }
}
