
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;

import seers.methspl.MethodDoc;
import seers.methspl.MethodSplitter;

public class CorpusGeneration {

	private static int numGeneratedFiles = 0;
	private static List<String> stopwordsList;

	/*
	 * 1. Lucene processing:
	 * numProcessFiles: 6063
	 * numGenerateFiles: 54667 vs 58034
	 * 
	 * 2. Eclipse.jdt.core processing:
	 * numProcessFiles: 6841: 
	 * numGenerateFiles: 66535 vs 67990
	 *  
	 * 
	 */

	public static void main(String[] args) throws IOException {

		System.out.println("main starts");
		
		stopwordsList = initStopwords();
		
		// Apache Lucene 
		String baseFolder = "/Users/sungsooahn/Downloads/lucene-solr-branch_5_4";

		// Eclipse.jdt.core 
		// String baseFolder = "/Users/sungsooahn/Downloads/eclipse.jdt.core-R4_5";
		
		
		File dir = new File(baseFolder);

		String[] extension = { "java" };
		Collection<File> found = FileUtils.listFiles(dir, extension, true);
		
		int numFile = 0;
		// int xxx = 0;
		for (File f : found) {
			// create the instance of the method splitter
			MethodSplitter splitter = new MethodSplitter(baseFolder);

			// file path of the java file that is going to be parsed and split
			// String filePath = baseFolder + File.separator + "JabRefDesktop.java";
			// split the java file and return the list of methods
			// List<MethodDoc> methods = splitter.splitIntoMethods(new File(filePath));

			System.out.println(numFile + ": " + f.getAbsolutePath());
			
			List<MethodDoc> methods = splitter.splitIntoMethods(f);

			// printMethods(f, methods);
			generateFileBasedOnMethods(f, methods);

			numFile++;

			/*
			if (xxx == 0)
				break;
			*/
			
		}
		
		System.out.println("numGenerateFiles: " + numGeneratedFiles);
		
	}

	private static void generateFileBasedOnMethods(File f, List<MethodDoc> methods) {
		
		for (MethodDoc methodDoc : methods) {
			
			String methodName = methodDoc.getName();
			methodName = methodName.replaceAll("[ ()]", "_");
			methodName += ".corpus";
			
			String txtElements = "" + methodDoc.getTxtElements();
			
			String preprocessedElements = doPreprocess(txtElements);
			
			String fileBasedOnMethod = f.getAbsolutePath() + methodName;

			File file = new File(fileBasedOnMethod);
			
			
			try {
				
				if (file.exists()) {
					// NOTE: There ARE some duplicate files!!
					System.out.println("Duplicate : " + fileBasedOnMethod);
				}
				else {
					numGeneratedFiles++;
				}
				
				// creates the file
				file.createNewFile();
				// creates a FileWriter Object
				FileWriter writer = new FileWriter(file);
				
				writer.write(preprocessedElements);
				
				writer.flush();
				writer.close();
	
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}

	private static String doPreprocess(String elements) {

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

	
    public static List<String> removeJavaKeyword(List<String> tokens) {
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

    public static List<String> initStopwords() {

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


    public static List<String> doStemming(List<String> tokens) {
    	
    	List<String> results = new ArrayList<String>();
    	
    	for (String t : tokens) {
    		Porter porter = new Porter();
    		String stemmed = porter.stripAffixes( t );  		
    	    	results.add(stemmed);
    	    
    	}
    	
    	return results; 
    }

	
	
    public static List<String> splitCamelCases(List<String> tokens) {
    	
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
    
    	
    	
    public static List<String> removeStopwords(List<String> tokens) {
    	
    	
    	
    	
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
    public static List<String> removeManyThings(List<String> tokens) {

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
