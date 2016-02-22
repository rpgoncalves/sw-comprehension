import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;


public class Preprocessor {

	
	private List<String> stopwordsList;

	public Preprocessor() {
		stopwordsList = initStopwords();
	}
	
	public String doPreprocess(String elements) {

		StrMatcher delim = StrMatcher.charSetMatcher(" ,:(){}<>[]\t\n/@");
        List<String> tokens = new StrTokenizer(elements, delim).getTokenList();

        List<String> tokens1 = removeJavaKeyword(tokens);
        // System.out.println("1: " + tokens1.toString());
        
        List<String> tokens3 = removeStopwords(tokens1);
        // System.out.println("3: " + tokens3.toString());
        		        
        List<String> tokens4 = splitCamelCases(tokens3);
        // System.out.println("4: " + tokens4.toString());
        
        List<String> tokens5 = doStemming(tokens4);
        // System.out.println("5: " + tokens5.toString());

        // remove number, two or fewer characters
        List<String> tokens2 = removeManyThings(tokens5);
        // System.out.println("2: " + tokens2.toString());

        String s = "";
        
        for (String t : tokens2) 
        	s = s + t + " ";
        
		return s;
		
	}

	
    private List<String> removeJavaKeyword(List<String> tokens) {
    	List<String> javaKeywordsList = 
    			Arrays.asList("abstract", "continue", "for", "new", "switch", 
		"assert", "default", "goto", "package", "synchronized",
		"boolean", "do", "if", "private", "this", 
		"break", "double", "implements", "protected", "throw",
		"byte", "else", "import", "public", "throws",
		"case",	"enum", "instanceof", "return", "transient",
		"catch", "extends", "int", "short", "try",
		"char", "final", "interface", "static", "void",
		"class", "finally", "long", "strictfp", "volatile",
		"const", "float", "native", "super", "while",
		// programming tokens included
		"{", "}", "[", "]", "<", ">");
    	
    	List<String> results = new ArrayList<String>();
    	
    	for (String t : tokens) {
    		
    		if (javaKeywordsList.contains(t)) {
				// remove the keyword 
				// do nothing
				// System.out.println("removed: " + t);    			
    		}
    		else {
				results.add(t);
    			
    		}
    	}
    	    	
    	return results; 
    	
    }

    private List<String> initStopwords() {

    	String stopwordFile = "/Users/sungsooahn/Documents/stoplist.dft";

    	List<String> stopwordList = new ArrayList<String>();
    			
    	try {
            BufferedReader in = new BufferedReader
            (new FileReader(stopwordFile));
            String str;
            String trimedStr;
            String wordTag = "<word>";
            while ((str = in.readLine()) != null) {
            	trimedStr = str.trim();
            	if (trimedStr.startsWith(wordTag)) {
            		int end = trimedStr.lastIndexOf("</word");
            		
            		String s = trimedStr.substring(wordTag.length(), end);
            		
            		stopwordList.add(s);
            		// System.out.println("stopword: " + s);
            	}
            }
            in.close();
        }
        catch (IOException e) {
		    System.err.format("IOException: %s%n", e);
        }

    	return stopwordList;
    	    	
    }


    private List<String> doStemming(List<String> tokens) {
    	
    	List<String> results = new ArrayList<String>();
    	
    	for (String t : tokens) {
    		Porter porter = new Porter();
    		String stemmed = porter.stripAffixes( t );  		
    	    	results.add(stemmed);
    	    
    	}
    	
    	return results; 
    }

	
	
    private List<String> splitCamelCases(List<String> tokens) {
    	
    	List<String> results = new ArrayList<String>();
    	
    	for (String t : tokens) {
    	    // String[] vars = t.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
    		String[] vars = t.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
    		
    		// if the token is CamelCase, add the original token
    		if (vars.length > 1)
    			results.add(t);
    		
    	    for (String s : vars)
    	    	results.add(s);
    	    
    	}  
    	
    	return results; 
    }
    
    	
    	
    private List<String> removeStopwords(List<String> tokens) {
    	
    	List<String> results = new ArrayList<String>();
    	
    	for (String t : tokens) {
    		
    		if (stopwordsList.contains(t)) {
				// remove the keyword 
				// do nothing
				// System.out.println("Stopword removed: " + t);    			
    		}
    		else {
				results.add(t);
    			
    		}
    	}
    	    	
    	return results; 
    	
    }

	
	
	
	// 1. Remove all terms that have two or fewer characters	
	// 2. Remove numbers
    private List<String> removeManyThings(List<String> tokens) {

    	List<String> results = new ArrayList<String>();
    	
       	for (String t : tokens) {
    		// 1. Remove all terms that have two or fewer characters
       		if (t.length() <= 2) {
				// System.out.println("t.length(): " + t.length() + "    removed: " + t);
    			continue;
    		}
       		
       		// 2. Remove numbers
       		if (StringUtils.isNumeric(t)) {
       			// System.out.println("Number removed: " + t);
       			continue;
       		}

       		results.add(t);
       		
       	}   	
       	
    	return results;
    }




}

